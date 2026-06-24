package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.event.annotation.BeforeTestMethod
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsDomainEvent
import java.util.Collections
import java.util.UUID

@Profile("test")
@Service
class TestSqsDomainEventListener(private val objectMapper: ObjectMapper) {

  private val log = LoggerFactory.getLogger(this::class.java)
  private val messages = Collections.synchronizedList(mutableListOf<HmppsDomainEvent>())

  fun takeMessageOrNull(eventTypeName: String, eventDescription: String, detailUrl: String?, cprAddressId: UUID? = null) = messages.singleOrNull {
    it.eventType == eventTypeName &&
      it.description == eventDescription &&
      it.detailUrl == detailUrl &&
      (cprAddressId == null || it.additionalInformation["corePersonAddressId"] == cprAddressId.toString())
  }?.also { messages.remove(it) }

  @Value("\${hmpps.sqs.topics.hmpps-domain-event-topic.arn}")
  lateinit var topicName: String

  @SqsListener(
    queueNames = ["test-domain-events-queue"],
    factory = "hmppsQueueContainerFactoryProxy",
    pollTimeoutSeconds = "1",
  )
  fun processMessage(rawMessage: String?) {
    val (message) = objectMapper.readValue(rawMessage, Message::class.java)
    val event = objectMapper.readValue(message, HmppsDomainEvent::class.java)

    log.info("Received Domain Event: $event")

    messages.add(event)
  }

  @BeforeTestMethod
  fun clearMessages() {
    messages.clear()
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageId") val messageId: String,
  @JsonProperty("MessageAttributes") val messageAttributes: MessageAttributes,
)

data class MessageAttributes(val eventType: EventType)

data class EventType(
  @JsonProperty("Value") val value: String,
  @JsonProperty("Type") val type: String,
)
