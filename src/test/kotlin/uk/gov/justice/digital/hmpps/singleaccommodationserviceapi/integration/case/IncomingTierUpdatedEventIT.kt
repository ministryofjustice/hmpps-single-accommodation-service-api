package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
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
  private val crn: String = "X123456"
  private val eventType = "tier.calculation.complete"
  private val eventDescription = "Tier calculation complete from Tier service"
  private val eventDetailUrl = "http://localhost:9994/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @AfterAll
  fun tearDown() {
    inboxEventRepository.deleteAll()
    outboxEventRepository.deleteAll()
    caseRepository.deleteAll()
  }

  @Test
  fun `should process incoming HMPPS TIER_CALCULATION_COMPLETE domain events on existing record`() {
    caseRepository.save(buildCaseEntity(crn = crn, tier = TierScore.A1))
    val tier = buildTier(tierScore = TierScore.A3)
    tierMockServer.stubGetCorePersonRecordOKResponse(
      crn,
      response = tier,
    )

    assertCaseEntityExistsAndIsNot(
      crn,
      expectedTier = tier,
    )

    // when
    publishTierEvent()

    // then
    assertPublishedSNSEvent(
      detailUrl = eventDetailUrl,
    )

    awaitDbRecordExists { assertThatMostRecentInboxRecordIsAsExpected() }

    awaitDbRecordExists {
      assertThat(caseRepository.findAll()).hasSize(1)
      val updatedCase = caseRepository.findByCrn(crn)
      assertThat(updatedCase?.tier?.name).isEqualTo(tier.tierScore.name)
    }
  }

  @Test
  fun `should process incoming HMPPS TIER_CALCULATION_COMPLETE domain events on new record`() {
    val tier = buildTier(tierScore = TierScore.A3)
    tierMockServer.stubGetCorePersonRecordOKResponse(
      crn,
      response = tier,
    )

    assertTheCaseTableIsEmpty()

    // when
    publishTierEvent()

    // then
    assertPublishedSNSEvent(
      detailUrl = eventDetailUrl,
    )

    awaitDbRecordExists { assertThatMostRecentInboxRecordIsAsExpected() }

    awaitDbRecordExists {
      assertThat(caseRepository.findAll()).hasSize(1)
      val updatedCase = caseRepository.findByCrn(crn)
      assertThat(updatedCase?.tier?.name).isEqualTo(tier.tierScore.name)
    }
  }

  @Test
  fun `should FAIL to process incoming HMPPS TIER_CALCULATION_COMPLETE domain event as callback URL fails with 404`() {
    tierMockServer.stubGetTierFailResponse(
      crn,
      externalId = externalId,
    )

    publishTierEvent()
    awaitDbRecordExists {
      assertThat(inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.FAILED)).isNotEmpty()
    }
    assertThat(caseRepository.findAll().size).isEqualTo(0)

    val inboxRecord = inboxEventRepository.findAll().first()
    assertThat(inboxRecord.eventType).isEqualTo(eventType)
    assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl)
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
        "detailUrl": "$eventDetailUrl", 
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

  private fun assertCaseEntityExistsAndIsNot(crn: String, expectedTier: Tier) {
    val persistedResult = caseRepository.findByCrn(crn)
    assertThat(persistedResult).isNotNull()
    assertThat(persistedResult?.tier?.name).isNotEqualTo(expectedTier.tierScore.name)
    assertThat(caseRepository.findAll()).hasSize(1)
  }

  private fun assertTheCaseTableIsEmpty() {
    assertThat(caseRepository.findAll()).hasSize(0)
  }

  private fun assertThatMostRecentInboxRecordIsAsExpected() {
    assertThat(inboxEventRepository.findAll()).hasSize(1)
    val inboxRecord =
      inboxEventRepository.findTopByOrderByCreatedAtDesc()
        ?: throw AssertionError("No inbox record found")
    val tierDomainEvent = jsonMapper.readValue(inboxRecord.payload, TierDomainEvent::class.java)
    val crnFromPayload = tierDomainEvent.personReference.findCrn()
    assertThat(crnFromPayload).isEqualTo(crn)

    assertThat(inboxRecord.eventType).isEqualTo(eventType)
    assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl)
    assertThat(inboxRecord.processedStatus).isEqualTo(ProcessedStatus.SUCCESS)
  }
}
