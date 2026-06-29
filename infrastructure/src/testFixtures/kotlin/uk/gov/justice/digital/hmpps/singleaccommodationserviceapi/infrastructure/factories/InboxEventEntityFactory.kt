package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@TestData
fun buildInboxEventEntity(
  eventType: String = "tier.calculation.changed",
  eventDetailUrl: String = "http://localhost/event",
  eventOccurredAt: OffsetDateTime = OffsetDateTime.now(),
  createdAt: Instant = Instant.now(),
  processedStatus: ProcessedStatus = ProcessedStatus.PENDING,
  processedAt: Instant? = Instant.now(),
  payload: String = "{}",
): InboxEventEntity = InboxEventEntity(
  id = UUID.randomUUID(),
  eventType = eventType,
  eventDetailUrl = eventDetailUrl,
  eventOccurredAt = eventOccurredAt,
  createdAt = createdAt,
  processedStatus = processedStatus,
  processedAt = processedAt,
  payload = payload,
)

@TestData
fun buildPendingInboxEventEntity(
  eventType: String = "tier.calculation.changed",
  eventDetailUrl: String = "http://localhost/event",
  eventOccurredAt: OffsetDateTime = OffsetDateTime.now(),
  payload: String = "{}",
) = buildInboxEventEntity(
  eventType = eventType,
  eventDetailUrl = eventDetailUrl,
  eventOccurredAt = eventOccurredAt,
  processedStatus = ProcessedStatus.PENDING,
  processedAt = null,
  payload = payload,

)
