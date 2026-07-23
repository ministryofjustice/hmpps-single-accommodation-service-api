package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(
  properties = [
    "scheduling.enabled=true",
    "case-refresh.worker.enabled=false",
  ],
)
class CaseProjectionRefreshHandlerIT : IntegrationTestBase() {

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var inboxEventRepository: InboxEventRepository

  @Autowired
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @Autowired
  lateinit var jsonMapper: JsonMapper

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic")
      ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  private lateinit var crn: String
  private val eventType = "tier.calculation.changed"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    databaseUtils.truncate(SAS_CASE, INBOX_EVENT)
    createSasSystemUser()
  }

  @Test
  fun `requests asynchronous Case projection refresh for an existing Case without calling upstream services`() {
    val originalCase = caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    publishProjectionChangeEvent()

    waitFor { assertThatSingleInboxEventHasStatus(ProcessedStatus.PROCESSED) }

    val persistedCase = caseRepository.findByCrn(crn)!!
    assertThat(persistedCase.tierScore).isEqualTo("A1")
    assertThat(persistedCase.cas1ApplicationId).isEqualTo(originalCase.cas1ApplicationId)
    assertThat(caseRefreshRequestRepository.findAll()).hasSize(1)
    sasWiremock.verify(0, getRequestedFor(urlPathMatching("/v[23]/crn/.*/tier")))
  }

  @Test
  fun `coalesces duplicate projection change events into one refresh request`() {
    caseRepository.save(buildCaseEntity(tierScore = "A1") { withCrn(crn) })

    publishProjectionChangeEvent()
    publishProjectionChangeEvent()

    inboxEventAsserter.assertAllInboxMessagesProcessed(2)
    val refreshRequest = caseRefreshRequestRepository.findAll().single()
    assertThat(refreshRequest.generation).isEqualTo(2)
    assertThat(caseRepository.findAll()).hasSize(1)
  }

  @Test
  fun `ignores a projection change event for an unknown Case`() {
    publishProjectionChangeEvent()

    waitFor { assertThatSingleInboxEventHasStatus(ProcessedStatus.IGNORED) }

    assertThat(caseRepository.findAll()).isEmpty()
    assertThat(caseRefreshRequestRepository.findAll()).isEmpty()
    sasWiremock.verify(0, getRequestedFor(urlPathMatching("/v[23]/crn/.*/tier")))
  }

  private fun publishProjectionChangeEvent() {
    val eventDetailUrl = "${applicationContext.environment.getProperty("service.tier.base-url")}/v3/crn/$crn/tier"
    val snsEvent = """
      {
        "eventType": "$eventType",
        "externalId": "${UUID.randomUUID()}",
        "version": 1,
        "description": "Tier calculation complete from Tier service",
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

  private fun assertThatSingleInboxEventHasStatus(processedStatus: ProcessedStatus) {
    val inboxEvents = inboxEventRepository.findAll()
    assertThat(inboxEvents).hasSize(1)
    val inboxEvent = inboxEvents.single()
    val domainEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    assertThat(domainEvent.personReference.findCrn()).isEqualTo(crn)
    assertThat(inboxEvent.eventType).isEqualTo(eventType)
    assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
  }
}
