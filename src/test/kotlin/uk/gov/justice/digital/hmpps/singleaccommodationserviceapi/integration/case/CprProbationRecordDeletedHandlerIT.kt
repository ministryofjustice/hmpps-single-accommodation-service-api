package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.DomainEventIntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshCompletionService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CprProbationRecordDeletedHandler
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class CprProbationRecordDeletedHandlerIT : DomainEventIntegrationTestBase() {

  @Autowired
  lateinit var caseRefreshCompletionService: CaseRefreshCompletionService

  @Autowired
  lateinit var cprProbationRecordDeletedHandler: CprProbationRecordDeletedHandler

  @Autowired
  lateinit var transactionManager: PlatformTransactionManager

  private lateinit var crn: String

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, buildCorePersonRecord())
    createSasSystemUser()
  }

  @Test
  fun `refresh completion does not undo archival committed by an overlapping deletion`() {
    val case = caseRepository.save(buildCaseEntity(tierScore = "A") { withCrn(crn) })
    caseRefreshRequestService.requestLiveRefresh(case.id)
    val claim = caseRefreshRequestService.claimPending(1, Duration.ofMinutes(10)).single()
    val inboxEvent = InboxEventHandler.InboxEvent(
      id = UUID.randomUUID(),
      eventDetailUrl = "localhost",
      payload = """
        {
          "eventType": "${IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED.typeName}",
          "version": 1,
          "description": "A probation record has been deleted",
          "detailUrl": "localhost",
          "occurredAt": "${OffsetDateTime.parse("2026-07-23T10:00:00Z")}",
          "personReference": {
            "identifiers": [
              { "type": "CRN", "value": "$crn" }
            ]
          }
        }
      """.trimIndent(),
    )

    assertThatThrownBy {
      TransactionTemplate(transactionManager).executeWithoutResult {
        caseRefreshCompletionService.completeRefresh(claim, projection())
        CompletableFuture.runAsync { cprProbationRecordDeletedHandler.handle(inboxEvent) }.get(10, TimeUnit.SECONDS)
      }
    }.isInstanceOf(ObjectOptimisticLockingFailureException::class.java)

    assertThat(caseRepository.findByCrn(crn)).isNull()
    assertThat(caseRepository.findCaseForSubjectAccessRequest(crn, null)!!.archived).isTrue()
  }

  private fun projection() = CaseMutationOrchestrationDto(
    crn = crn,
    cpr = null,
    tier = buildTier(tierScore = "B"),
    prisoner = null,
    cas1CurrentPremises = null,
    cas3CurrentPremises = null,
    cas1Application = null,
    cas3Application = null,
    case = null,
  )
}
