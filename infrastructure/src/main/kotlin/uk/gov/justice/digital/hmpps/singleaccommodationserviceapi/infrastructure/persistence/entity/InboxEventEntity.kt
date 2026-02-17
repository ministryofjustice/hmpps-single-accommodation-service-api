package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.net.URI
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "inbox_event")
class InboxEventEntity(
  @Id
  val id: UUID,
  val eventType: String,
  val eventDetailUrl: String?,
  val eventOccurredAt: OffsetDateTime,
  val createdAt: Instant,
  @Enumerated(EnumType.STRING)
  var processedStatus: ProcessedStatus,
  var processedAt: Instant?,
  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var payload: String,
)

fun InboxEventEntity.uri(): URI = URI.create(requireNotNull(eventDetailUrl) { "Missing detail url" })
