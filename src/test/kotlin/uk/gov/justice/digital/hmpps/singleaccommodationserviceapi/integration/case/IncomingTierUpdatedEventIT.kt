package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTierV3
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierEventHandlerConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.OUTBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class IncomingTierUpdatedEventIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  @Autowired
  lateinit var tierEventHandlerConfig: TierEventHandlerConfig

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic")
      ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String
  private val eventType = "tier.calculation.changed"
  private val eventDescription = "Tier calculation complete from Tier service"
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v3/crn/$crn/tier"

  @AfterAll
  fun tearDown() {
    tierEventHandlerConfig.v3Enabled = false
  }

  @Nested
  inner class TierV2 {
    @BeforeEach
    fun setup() {
      tierEventHandlerConfig.v3Enabled = false
      crn = UUID.randomUUID().toString()
      HmppsAuthStubs.stubGrantToken()
      databaseUtils.truncate(SAS_CASE, DUTY_TO_REFER, OUTBOX_EVENT, INBOX_EVENT)
    }

    @Test
    fun `should process incoming HMPPS TIER_CALCULATION_CHANGED domain events on existing record`() {
      caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })
      val tier = buildTier(tierScore = "A3")
      TierStubs.getTierOKResponse(crn, response = tier)

      publishTierEvent()

      waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.PROCESSED) }

      val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
      assertThat(case.tierScore).isEqualTo("A3")
    }

    @Test
    fun `process multiple incoming HMPPS TIER_CALCULATION_CHANGED domain events for the same CRN, updating the same database row`() {
      databaseUtils.truncate(INBOX_EVENT)
      caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

      TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A2"))
      publishTierEvent()

      inboxEventAsserter.assertAllInboxMessagesProcessed(1)

      val caseUpdate1 = caseRepository.findByIdentifier(crn, IdentifierType.CRN)!!
      assertThat(caseUpdate1.tierScore).isEqualTo("A2")

      TierStubs.getTierOKResponse(crn, response = buildTier(tierScore = "A3"))
      publishTierEvent()

      inboxEventAsserter.assertAllInboxMessagesProcessed(2)

      val caseUpdate2 = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
      assertThat(caseUpdate2.tierScore).isEqualTo("A3")

      assertThat(caseRepository.findAll()).hasSize(1)
    }

    @Test
    fun `should not process incoming HMPPS TIER_CALCULATION_CHANGED domain events on unknown record`() {
      val tier = buildTier(tierScore = "A3")
      TierStubs.getTierOKResponse(crn, response = tier)

      assertThat(caseRepository.findAll()).hasSize(0)

      publishTierEvent()

      waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.PROCESSED) }
      assertThat(caseRepository.findAll()).hasSize(0)
    }

    @Test
    fun `should FAIL to process incoming HMPPS TIER_CALCULATION_CHANGED domain event as callback URL fails with 404`() {
      TierStubs.getTierNotFoundResponse(
        crn,
      )

      publishTierEvent()

      waitFor {
        assertThat(caseRepository.findAll().size).isEqualTo(0)
        assertThatSingleInboxEventIsAsExpected(ProcessedStatus.FAILED)
        assertThat(
          inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.FAILED, Pageable.unpaged()),
        ).isNotEmpty()
      }
      assertThat(caseRepository.findAll().size).isEqualTo(0)

      val inboxRecord = inboxEventRepository.findAll().first()
      assertThat(inboxRecord.eventType).isEqualTo(eventType)
      assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl())
      assertThat(inboxRecord.processedStatus).isEqualTo(ProcessedStatus.FAILED)
    }
  }

  @Nested
  inner class TierV3 {

    @BeforeEach
    fun setup() {
      crn = UUID.randomUUID().toString()
      HmppsAuthStubs.stubGrantToken()
      databaseUtils.truncate(SAS_CASE, DUTY_TO_REFER, OUTBOX_EVENT, INBOX_EVENT)
      tierEventHandlerConfig.v3Enabled = true
    }

    @Test
    fun `should process incoming HMPPS TIER_CALCULATION_CHANGED domain events on existing record`() {
      caseRepository.save(buildCaseEntity(tierScore = "A") { withCrn(crn) })
      val tier = buildTierV3(tierScore = "B")
      TierStubs.getTierOKResponseV3(crn, response = tier)

      publishTierEvent()

      waitFor { assertThatSingleInboxEventIsAsExpected(ProcessedStatus.PROCESSED) }

      val case = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }
      assertThat(case.tierScore).isEqualTo("B")
    }

    @Test
    fun `process multiple incoming HMPPS TIER_CALCULATION_CHANGED domain events for the same CRN, updating the same database row`() {
      caseRepository.saveAndFlush(buildCaseEntity(tierScore = "A") { withCrn(crn) })

      TierStubs.getTierOKResponseV3(crn, response = buildTierV3(tierScore = "B"))
      publishTierEvent()

      inboxEventAsserter.assertAllInboxMessagesProcessed(1)

      val caseUpdate1 = caseRepository.findByIdentifier(crn, IdentifierType.CRN)!!
      assertThat(caseUpdate1.tierScore).isEqualTo("B")

      TierStubs.getTierOKResponseV3(crn, response = buildTierV3(tierScore = "C"))
      publishTierEvent()
      inboxEventAsserter.assertAllInboxMessagesProcessed(2)

      val caseUpdate2 = waitForEntity { caseRepository.findByIdentifier(crn, IdentifierType.CRN) }

      assertThat(caseUpdate2.tierScore).isEqualTo("C")

      assertThat(caseRepository.findAll()).hasSize(1)
    }

    @Test
    fun `should not process incoming HMPPS TIER_CALCULATION_CHANGED domain events on unknown record`() {
      val tier = buildTierV3(tierScore = "A")
      TierStubs.getTierOKResponseV3(crn, response = tier)

      assertThat(caseRepository.findAll()).hasSize(0)

      publishTierEvent()

      inboxEventAsserter.assertAllInboxMessagesProcessed(1)
      assertThat(caseRepository.findAll()).hasSize(0)
    }

    @Test
    fun `should FAIL to process incoming HMPPS TIER_CALCULATION_CHANGED domain event as callback URL fails with 404`() {
      TierStubs.getTierNotFoundResponseV3(crn)

      publishTierEvent()

      waitFor {
        assertThat(caseRepository.findAll().size).isEqualTo(0)
        assertThatSingleInboxEventIsAsExpected(ProcessedStatus.FAILED)
        assertThat(
          inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.FAILED, Pageable.unpaged()),
        ).isNotEmpty()
      }
      assertThat(caseRepository.findAll().size).isEqualTo(0)

      val inboxRecord = inboxEventRepository.findAll().first()
      assertThat(inboxRecord.eventType).isEqualTo(eventType)
      assertThat(inboxRecord.eventDetailUrl).isEqualTo(eventDetailUrl())
      assertThat(inboxRecord.processedStatus).isEqualTo(ProcessedStatus.FAILED)
    }
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
    val tierDomainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    assertThat(tierDomainEvent.personReference.findCrn()).isEqualTo(crn)

    assertThat(inboxEvent.eventType).isEqualTo(eventType)
    assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl())
    assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
