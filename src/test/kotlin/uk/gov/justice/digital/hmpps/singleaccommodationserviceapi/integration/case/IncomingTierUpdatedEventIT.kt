package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.TierDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class IncomingTierUpdatedEventIT : IntegrationTestBase() {

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
  private val eventType = "tier.calculation.complete"
  private val eventDescription = "Tier calculation complete from Tier service"
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuth.stubGrantToken()
    deleteAllFromRepositories()
  }

  @AfterAll
  fun tearDown() {
    deleteAllFromRepositories()
  }

  private fun deleteAllFromRepositories() {
    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @Test
  fun `should process incoming HMPPS TIER_CALCULATION_COMPLETE domain events on existing record`() {
    caseRepository.save(buildCaseEntity(crn = crn, tier = TierScore.A1))
    val tier = buildTier(tierScore = TierScore.A3)
    TierStubs.getTierOKResponse(crn, response = tier)

    // when
    publishTierEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())

    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.SUCCESS) }

    val case = waitForEntity { caseRepository.findByCrn(crn) }
    assertThat(case.tier).isEqualTo(TierScore.A3)
  }

  @Test
  fun `should process incoming HMPPS TIER_CALCULATION_COMPLETE domain events on new record`() {
    val tier = buildTier(tierScore = TierScore.A3)
    TierStubs.getTierOKResponse(crn, response = tier)

    assertThat(caseRepository.findAll()).hasSize(0)

    // when
    publishTierEvent()

    // then
    assertPublishedSNSEvent(detailUrl = eventDetailUrl())

    waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.SUCCESS) }

    val case = waitForEntity { caseRepository.findByCrn(crn) }
    assertThat(case.tier!!.name).isEqualTo(tier.tierScore.name)
  }

  @Test
  fun `should FAIL to process incoming HMPPS TIER_CALCULATION_COMPLETE domain event as callback URL fails with 404`() {
    TierStubs.getTierFailResponse(
      crn,
      externalId = externalId,
    )

    publishTierEvent()

    waitFor {
      assertThat(caseRepository.findAll().size).isEqualTo(0)
      assertThatSingleInboxEventIsAsExpected(ProcessedStatus.FAILED)
      assertThat(
        inboxEventRepository.findAllByProcessedStatus(
          ProcessedStatus.FAILED,
          PageRequest.of(
            0,
            10,
            Sort.by("eventOccurredAt").ascending(),
          ),
        ),
      ).isNotEmpty()
    }
    assertThat(caseRepository.findAll().size).isEqualTo(0)

    val inboxRecord = inboxEventRepository.findAll().first()
    assertThat(inboxRecord.eventType).isEqualTo(eventType)
    assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl())
    assertThat(inboxRecord.processedStatus).isEqualTo(ProcessedStatus.FAILED)
  }

  private fun assertPublishedSNSEvent(
    detailUrl: String,
  ) {
    val emittedMessage = testSqsDomainEventListener.blockForMessage(IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE)
    assertThat(emittedMessage.description).isEqualTo(IncomingHmppsDomainEventType.TIER_CALCULATION_COMPLETE.typeDescription)
    assertThat(emittedMessage.detailUrl).isEqualTo(detailUrl)
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
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, TierDomainEvent::class.java)
    assertThat(tierDomainEvent.personReference.findCrn()).isEqualTo(crn)

    assertThat(inboxEvent.eventType).isEqualTo(eventType)
    assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl())
    assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
