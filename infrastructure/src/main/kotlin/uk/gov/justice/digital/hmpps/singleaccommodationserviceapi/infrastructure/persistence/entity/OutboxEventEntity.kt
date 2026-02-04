package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox_event")
data class OutboxEventEntity(
  @Id
  val id: UUID,
  val aggregateId: UUID,
  val aggregateType: String,
  val domainEventType: String,
  val payload: String,
  val createdAt: Instant,
  @Enumerated(EnumType.STRING)
  var processedStatus: ProcessedStatus,
  val processedAt: Instant?,
)
