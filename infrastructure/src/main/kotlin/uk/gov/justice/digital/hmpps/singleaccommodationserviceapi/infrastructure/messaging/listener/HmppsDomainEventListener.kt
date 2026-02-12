package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.listener

import com.fasterxml.jackson.annotation.JsonProperty
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsSnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Instant
import java.util.UUID

@Profile(value = ["local", "dev", "test"])
@Component
class HmppsDomainEventListener(
  private val jsonMapper: JsonMapper,
  private val inboxEventRepository: InboxEventRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("sas-domain-events-queue", factory = "hmppsQueueContainerFactoryProxy")
  fun processMessage(msg: String) {
    try {
      val (message) = jsonMapper.readValue(msg, SQSMessage::class.java)
      val event = jsonMapper.readValue(message, HmppsSnsDomainEvent::class.java)
      inboxEventRepository.save(
        InboxEventEntity(
          id = UUID.randomUUID(),
          eventType = event.eventType,
          eventDetailUrl = event.detailUrl,
          eventOccurredAt = event.occurredAt,
          createdAt = Instant.now(),
          processedStatus = ProcessedStatus.PENDING,
          processedAt = null,
          payload = jsonMapper.writeValueAsString(event),
        ),
      )
    } catch (e: Exception) {
      log.error("Exception caught in HmppsDomainEventListener", e)
      throw e
    }
  }

  data class SQSMessage(
    @JsonProperty("Message") val message: String,
  )
}
