package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
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

  lateinit var crn: String
  private val eventType = IncomingHmppsDomainEventType.CASE_ALLOCATED
  private fun eventDetailUrl() = "/we.ignore.this"

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
  fun `should process incoming CASE_ALLOCATED domain events`() {
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn)))
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = caseSummaries)

    // when
    publishCaseAllocatedEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())

    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.PROCESSED) }

    val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
    assertThat(case.tierScore).isEqualTo(TierScore.A3)
  }

  private fun assertPublishedSNSEvent(
    detailUrl: String,
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE)
    assertThat(emittedMessage.description).isEqualTo(IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo(detailUrl)
  }

  private fun publishCaseAllocatedEvent() {
    val snsEvent = """ 
      {
        "eventType": "${eventType.typeName}",
        "version": 1,
        "description": "${eventType.typeDescription}",
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
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType.typeName).build(),
          ),
        ).build(),
    )
  }

  private fun assertThatSingleInboxEventIsAsExpected(processedStatus: ProcessedStatus) {
    val inboxEvents = inboxEventRepository.findAll()
    assertThat(inboxEvents).hasSize(1)
    val inboxEvent = inboxEvents.first()
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    assertThat(tierDomainEvent.personReference.findCrn()).isEqualTo(crn)

    assertThat(inboxEvent.eventType).isEqualTo(eventType.typeName)
    assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl())
    assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
