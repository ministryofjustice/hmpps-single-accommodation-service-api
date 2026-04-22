package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CaseAllocationHandlerIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var outboxEventRepository: OutboxEventRepository

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @Autowired
  lateinit var jsonMapper: JsonMapper

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic") ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String

  private val eventType = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName
  private val eventDescription = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeDescription
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    deleteAllFromRepositories()
  }

  private fun deleteAllFromRepositories() {
    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when new case`() {
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when case already exists - this is duplicate event scenario to prove idempotency`() {
    caseRepository.save(buildCaseEntity(tierScore = TierScore.A3) { withCrn(crn) })
    shouldProcessCaseAllocationEventSuccessfully()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as FAILED when CPR call fails (as we need CPR identifiers to ensure not creating duplicates)`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = TierScore.A3))

    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = Cas1PlacementStatus.ARRIVED,
      requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
    )
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = cas1Application)

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.FAILED) }
    assertThat(caseRepository.findByIdentifier(crn, IdentifierType.CRN)).isNull()
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
    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.NOT_PROCESSED) }
    assertThat(caseRepository.findByIdentifier(crn, IdentifierType.CRN)).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED when tier and cas API calls fail`() {
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn,
      buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn))),
    )
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierServerErrorResponse(crn)
    ApprovedPremisesStubs.getCas1SuitableApplicationServerErrorResponse(crn = crn)

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())
    assertSuccessful(
      expectedCas1Application = null,
      expectedTierScore = null,
    )
  }

  private fun shouldProcessCaseAllocationEventSuccessfully() {
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn,
      buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn))),
    )
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn))))
    TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = TierScore.A3))

    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = Cas1PlacementStatus.ARRIVED,
      requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
    )
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = cas1Application)

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())

    assertSuccessful(cas1Application)
  }

  private fun assertSuccessful(
    expectedCas1Application: Cas1Application?,
    expectedTierScore: TierScore? = TierScore.A3,
  ) {
    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.PROCESSED) }
    val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
    if (expectedCas1Application != null) {
      assertThat(case.tierScore).isEqualTo(expectedTierScore)
    } else {
      assertThat(case.tierScore).isNull()
    }
    if (expectedCas1Application != null) {
      assertThat(case.cas1ApplicationId).isEqualTo(expectedCas1Application.id)
      assertThat(case.cas1ApplicationApplicationStatus).isEqualTo(expectedCas1Application.applicationStatus)
      assertThat(case.cas1ApplicationRequestForPlacementStatus).isEqualTo(expectedCas1Application.requestForPlacementStatus)
      assertThat(case.cas1ApplicationPlacementStatus).isEqualTo(expectedCas1Application.placementStatus)
    } else {
      assertThat(case.cas1ApplicationId).isNull()
      assertThat(case.cas1ApplicationApplicationStatus).isNull()
      assertThat(case.cas1ApplicationRequestForPlacementStatus).isNull()
      assertThat(case.cas1ApplicationPlacementStatus).isNull()
    }
  }

  private fun assertPublishedSNSEvent(
    detailUrl: String,
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(IncomingHmppsDomainEventType.CASE_ALLOCATED)
    assertThat(emittedMessage.description).isEqualTo(IncomingHmppsDomainEventType.CASE_ALLOCATED.typeDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo(detailUrl)
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

    domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(snsEvent)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        ).build(),
    )
  }

  private fun assertThatSingleInboxEventIsAsExpected(processedStatus: ProcessedStatus) {
    val inboxEvents = inboxEventRepository.findAll()
    assertThat(inboxEvents).hasSize(1)
    val inboxEvent = inboxEvents.first()
    val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    assertThat(caseAllocationEvent.personReference.findCrn()).isEqualTo(crn)

    assertThat(inboxEvent.eventType).isEqualTo(eventType)
    assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl())
    assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
