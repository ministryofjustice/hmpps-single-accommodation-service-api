package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.awspring.cloud.sqs.annotation.SqsListener
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.event.annotation.BeforeTestMethod
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsDomainEvent
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.Collections
import java.util.UUID

@Profile("test")
@Service
class TestSqsDomainEventListener(private val jsonMapper: JsonMapper) {

  private val log = LoggerFactory.getLogger(this::class.java)
  private val messages = Collections.synchronizedList(mutableListOf<HmppsDomainEvent>())

  fun assertQueueIsEmpty() {
    log.info("Asserting queue is empty: {}", messages.size)
    await
      .logging()
      .atMost(ofSeconds(5))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        assertThat(messages).isEmpty()
      }
  }

  fun assertMessageReceived(
    typeName: String,
    eventDescription: String,
    detailUrl: String?,
    cprAddressId: UUID? = null,
  ): HmppsDomainEvent {
    var matchedMessage: HmppsDomainEvent? = null

    await
      .atMost(ofSeconds(5))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        matchedMessage = takeMessageOrNull(typeName, eventDescription, detailUrl, cprAddressId)
        assertThat(matchedMessage).isNotNull()
      }
    return matchedMessage!!
  }

  fun takeMessageOrNull(
    eventTypeName: String,
    eventDescription: String,
    detailUrl: String?,
    cprAddressId: UUID? = null,
  ): HmppsDomainEvent? = synchronized(messages) {
    messages.singleOrNull {
      it.eventType == eventTypeName &&
        it.description == eventDescription &&
        it.detailUrl == detailUrl &&
        (
          cprAddressId == null ||
            it.additionalInformation?.let { ai ->
              ai["corePersonAddressId"]
            } == cprAddressId.toString()
          )
    }?.also { messages.remove(it) }
  }

  @Value("\${hmpps.sqs.topics.hmpps-domain-event-topic.arn}")
  lateinit var topicName: String

  @SqsListener(
    queueNames = ["test-domain-events-queue"],
    factory = "hmppsQueueContainerFactoryProxy",
    pollTimeoutSeconds = "1",
  )
  fun processMessage(rawMessage: String?) {
    val (message) = jsonMapper.readValue(rawMessage, Message::class.java)
    val event = jsonMapper.readValue(message, HmppsDomainEvent::class.java)

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
