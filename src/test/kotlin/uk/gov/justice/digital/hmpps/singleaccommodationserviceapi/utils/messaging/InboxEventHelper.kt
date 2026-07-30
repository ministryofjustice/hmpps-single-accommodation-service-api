package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingTopicException
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

@Component
class InboxEventHelper(
  private val inboxEventRepository: InboxEventRepository,
  private val jsonMapper: JsonMapper,
  private val hmppsQueueService: HmppsQueueService,
) {
  private val domainTopic by lazy {
    hmppsQueueService.findByTopicId("hmpps-domain-event-topic")
      ?: throw MissingTopicException("hmpps-domain-event-topic topic not found")
  }
  fun assertAllInboxMessagesProcessed(count: Int) {
    assertExpectedInboxEvents(ProcessedStatus.PROCESSED, count)
  }

  fun assertMessageProcessed() = assertAllInboxMessagesProcessed(1)

  fun assertExpectedInboxEvents(processedStatus: ProcessedStatus, count: Int) {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        assertThat(
          inboxEventRepository.findAllByProcessedStatus(processedStatus, Pageable.unpaged()),
        ).hasSize(count)
      }
  }

  fun publish(message: String, eventType: String) {
    domainTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainTopic.arn)
        .message(message)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        ).build(),
    )
  }

  fun assertInboxEvent(
    crn: String,
    eventType: String,
    eventDetailUrl: String?,
    processedStatus: ProcessedStatus,
  ) {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        val inboxEvents = inboxEventRepository.findAll()
        assertThat(inboxEvents).hasSize(1)
        val inboxEvent = inboxEvents.first()
        val event = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
        assertThat(event.personReference.findCrn()).isEqualTo(crn)
        assertThat(inboxEvent.eventType).isEqualTo(eventType)
        assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl)
        assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
      }
  }
}
