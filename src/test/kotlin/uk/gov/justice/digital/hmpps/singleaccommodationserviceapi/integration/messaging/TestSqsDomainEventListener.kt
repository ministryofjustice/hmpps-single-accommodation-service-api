package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.junit.jupiter.api.Assertions.fail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.event.annotation.BeforeTestMethod
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsSnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import java.time.Duration

@Profile("test")
@Service
class TestSqsDomainEventListener(private val objectMapper: ObjectMapper) {
  private val log = LoggerFactory.getLogger(this::class.java)
  private val messages = mutableListOf<HmppsSnsDomainEvent>()

  @Value("\${hmpps.sqs.topics.hmpps-domain-event-topic.arn}")
  lateinit var topicName: String

  @SqsListener(queueNames = ["test-domain-events-queue"], factory = "hmppsQueueContainerFactoryProxy", pollTimeoutSeconds = "1")
  fun processMessage(rawMessage: String?) {
    val (message) = objectMapper.readValue(rawMessage, Message::class.java)
    val event = objectMapper.readValue(message, HmppsSnsDomainEvent::class.java)

    log.info("Received Domain Event: $event")
    synchronized(messages) {
      messages.add(event)
    }
  }

  @BeforeTestMethod
  fun clearMessages() {
    messages.clear()
  }

  fun blockForMessage(eventType: SingleAccommodationServiceDomainEventType): HmppsSnsDomainEvent {
    val typeName = eventType.typeName
    var waitedCount = 0
    while (!contains(typeName)) {
      if (waitedCount >= Duration.ofSeconds(15).toMillis()) {
        fail<Any>("Did not receive SQS message of type $eventType from SNS topic $topicName after 15s. Have messages of type ${messages.map { m -> m.eventType }}")
      }

      Thread.sleep(100)
      waitedCount += 100
    }

    synchronized(messages) {
      return messages.first { it.eventType == typeName }
    }
  }

  private fun contains(eventType: String): Boolean {
    synchronized(messages) {
      return messages.firstOrNull { it.eventType == eventType } != null
    }
  }
}

// Warning suppressed because we have to match the SNS attribute naming case
@SuppressWarnings("ConstructorParameterNaming")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
  val Message: String,
  val MessageId: String,
  val MessageAttributes: MessageAttributes,
)

data class MessageAttributes(val eventType: EventType)

// Warning suppressed because we have to match the SNS attribute naming case
@SuppressWarnings("ConstructorParameterNaming")
data class EventType(val Value: String, val Type: String)
