package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.INBOX_EVENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@TestPropertySource(properties = ["scheduling.enabled=true"])
class CaseAllocatedEventIT : IntegrationTestBase() {

  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic") ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  private val externalId: UUID = UUID.fromString("0418d8b8-3599-4224-9a69-49af02f806c5")
  lateinit var crn: String

  private val eventType = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName
  private val eventDescription = IncomingHmppsDomainEventType.CASE_ALLOCATED.typeDescription
  private fun eventDetailUrl() = "${applicationContext.environment.getProperty("service.tier.base-url")}/v2/crn/$crn/tier"

  @BeforeEach
  fun setup() {
    crn = UUID.randomUUID().toString()
    HmppsAuthStubs.stubGrantToken()
    databaseUtils.truncate(SAS_CASE, INBOX_EVENT, DUTY_TO_REFER)
    cacheManager.setCacheNames(listOf(GET_CASE_LIST))
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as PROCESSED and remove entries from cache`() {
    cacheManager.setCacheNames(listOf(GET_CASE_LIST))
    cacheManager.getCache(GET_CASE_LIST)!!.put(USERNAME_OF_LOGGED_IN_DELIUS_USER, listOf(buildCase()))

    assertThat(cacheManager.getCache(GET_CASE_LIST)!!.get(USERNAME_OF_LOGGED_IN_DELIUS_USER)).isNotNull

    // when
    publishCaseAllocatedEvent()

    // then
    inboxEventAsserter.assertAllInboxMessagesProcessed(1)
    inboxEventAsserter.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(),
      processedStatus = ProcessedStatus.PROCESSED,
    )
    assertThat(cacheManager.getCache(GET_CASE_LIST)!!.get(USERNAME_OF_LOGGED_IN_DELIUS_USER)).isNull()
  }

  @Test
  fun `should process incoming CASE_ALLOCATED domain event as FAILED when no cache available`() {
    cacheManager.removeCache(GET_CASE_LIST)

    // when
    publishCaseAllocatedEvent()

    // then
    inboxEventAsserter.assertInboxEvent(
      crn = crn,
      eventType = eventType,
      eventDetailUrl = eventDetailUrl(),
      processedStatus = ProcessedStatus.FAILED,
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
}
