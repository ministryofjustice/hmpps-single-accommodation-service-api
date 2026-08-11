package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierEventHandlerConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class TierCalculationChangedIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var tierEventHandlerConfig: TierEventHandlerConfig

  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String
  private val eventType = "tier.calculation.changed"
  private val eventDescription = "Tier calculation complete from Tier service"
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v3/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
    createSasSystemUser()
  }

  @Test
  fun `should process incoming HMPPS TIER_CALCULATION_CHANGED domain events on existing record`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
    val tier = buildTier(tierScore = "A3")
    TierStubs.getTierOKResponse(crn, response = tier)

    publishTierEvent()

    testInboxEventHelper.assertMessageProcessed()
    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl(), ProcessedStatus.PROCESSED)

    waitFor { assertThat(caseRefreshRequestRepository.findAll()).hasSize(1) }
  }

  @Test
  fun `process multiple incoming HMPPS TIER_CALCULATION_CHANGED domain events for the same CRN`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A2"))
    publishTierEvent()
    publishTierEvent()
    publishTierEvent()

    testInboxEventHelper.assertAllInboxMessagesProcessed(3)

    waitFor {
      val case = caseRefreshRequestRepository.findAll()
      assertThat(case.single().generation).isEqualTo(3)
    }
  }

  @Test
  fun `should not process incoming HMPPS TIER_CALCULATION_CHANGED domain events on unknown record`() {
    val tier = buildTier(tierScore = "A3")
    TierStubs.getTierOKResponse(crn, response = tier)

    assertThat(caseRepository.findAll()).hasSize(0)

    publishTierEvent()

    testInboxEventHelper.assertInboxEvent(crn, eventType, eventDetailUrl(), ProcessedStatus.IGNORED)
    assertThat(caseRefreshRequestRepository.findAll()).hasSize(0)
  }

  private fun publishTierEvent() {
    val snsEvent = """ 
      {
        "eventType": "$eventType",
        "externalId": "$externalId",
        "version": 1,
        "description": "$eventDescription",
        "detailUrl": "${eventDetailUrl()}", 
        "personReference": {
           "identifiers": [
              {
                "type": "CRN", 
                "value": "$crn"
               }
            ]
        },
        "occurredAt": "${Instant.now().atOffset(ZoneOffset.UTC)}"
      }
    """.trimIndent()

    testInboxEventHelper.publish(snsEvent, eventType)
  }
}
