package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CaseAllocatedEventIT : IntegrationTestBase() {
  @Autowired
  lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String

  private val eventType = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName
  private val eventDescription = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeDescription
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v2/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(SAS_CASE, DUTY_TO_REFER, INBOX_EVENT, OUTBOX_EVENT)
    createSasSystemUser()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when new case`() {
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when case already exists - this is duplicate event scenario to prove idempotency`() {
    caseRepository.save(buildCaseEntity(tierScore = "A3") { withCrn(crn) })
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as FAILED when CPR call fails (as we need CPR identifiers to ensure not creating duplicates)`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A3"))

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    inboxEventHelper.assertMessageProcessed()

    val case = caseRepository.findByIdentifier(crn, IdentifierType.CRN)
    assertThat(case).isNotNull()
    assertThat(case?.firstName).isNull()
    assertThat(case?.lastName).isNull()
    assertThat(case?.dateOfBirth).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as NOT_PROCESSED when case is NOT allocated to a SAS onboarded team`() {
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(
      response = CaseSummaries(
        listOf(
          buildCaseSummary(
            crn = crn,
            manager = buildManager(
              team = buildTeam(
                code = "NOT_ONBOARDED",
              ),
            ),
          ),
        ),
      ),
    )

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    inboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
    assertThat(caseRepository.findByIdentifier(crn, IdentifierType.CRN)).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when tier API call fails`() {
    val cpr = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, cpr)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierServerErrorResponse(crn)

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    assertSuccessful(
      expectedTier = null,
      cpr = cpr,
    )
  }

  private fun shouldProcessCaseAllocationEventSuccessfully() {
    val cpr = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn, cpr)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A3"))

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())

    assertSuccessful(cpr = cpr)
  }

  private fun assertSuccessful(
    expectedTier: String? = "A3",
    cpr: CorePersonRecord? = null,
  ) {
    inboxEventHelper.assertMessageProcessed()

    val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
    assertThat(case.tierScore).isEqualTo(expectedTier)
    assertThat(case.firstName).isEqualTo(cpr?.firstName)
    assertThat(case.lastName).isEqualTo(cpr?.lastName)
    assertThat(case.dateOfBirth).isEqualTo(cpr?.dateOfBirth)
  }

  private fun assertPublishedSNSEvent(
    detailUrl: String,
  ) {
    testSqsDomainEventListener.assertMessageReceived(
      typeName = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName,
      eventDescription = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeDescription,
      detailUrl = detailUrl,
    )
  }

  private fun publishCaseAllocatedEvent() {
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

    inboxEventHelper.publish(snsEvent, eventType)
  }
}
