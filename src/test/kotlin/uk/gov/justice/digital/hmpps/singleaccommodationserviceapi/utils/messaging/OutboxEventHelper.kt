package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.awaitility.kotlin.await
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.UUID

@Component
class OutboxEventHelper(
  private val outboxEventTestRepository: OutboxEventTestRepository,
) {

  fun waitForMessage(
    aggregateId: UUID,
    aggregateType: String,
    eventType: SingleAccommodationServiceDomainEventType,
    processedStatus: ProcessedStatus,
  ): OutboxEventEntity {
    var outboxRecord: OutboxEventEntity? = null
    await
      .atMost(ofSeconds(5))
      .pollInterval(ofMillis(100))
      .until {
        outboxRecord = outboxEventTestRepository.findByAggregateIdAndAggregateTypeAndDomainEventTypeAndProcessedStatus(
          aggregateId,
          aggregateType,
          eventType.name,
          processedStatus,
        )
        outboxRecord != null
      }
    return outboxRecord!!
  }
}

@Repository
interface OutboxEventTestRepository : JpaRepository<OutboxEventEntity, UUID> {
  fun findByAggregateIdAndAggregateTypeAndDomainEventTypeAndProcessedStatus(
    aggregateId: UUID,
    aggregateType: String,
    domainEventType: String,
    processedStatus: ProcessedStatus,
  ): OutboxEventEntity?
}
