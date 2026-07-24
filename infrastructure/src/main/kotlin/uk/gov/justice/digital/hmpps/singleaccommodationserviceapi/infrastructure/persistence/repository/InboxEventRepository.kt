package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.time.Instant
import java.util.UUID

interface InboxEventRepository : JpaRepository<InboxEventEntity, UUID> {
  fun findAllByProcessedStatus(processedStatus: ProcessedStatus, pageable: Pageable): List<InboxEventEntity>

  @Query(
    value =
    """
      SELECT id
      FROM inbox_event
      WHERE processed_status = 'PENDING'
      ORDER BY event_occurred_at ASC
      LIMIT :maxSize
      FOR UPDATE SKIP LOCKED
      """,
    nativeQuery = true,
  )
  fun lockPendingIds(@Param("maxSize") maxSize: Int): List<UUID>

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
    """
    UPDATE InboxEventEntity event
    SET event.processedStatus = :newStatus,
        event.processedAt = :processedAt
    WHERE event.id IN :ids
      AND event.processedStatus = :expectedStatus
    """,
  )
  fun updateProcessedStatusForIds(
    @Param("ids") ids: List<UUID>,
    @Param("expectedStatus") expectedStatus: ProcessedStatus,
    @Param("newStatus") newStatus: ProcessedStatus,
    @Param("processedAt") processedAt: Instant,
  ): Int

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
    """
    UPDATE InboxEventEntity event
    SET event.processedStatus = :newStatus,
        event.processedAt = null
    WHERE event.processedStatus = :expectedStatus
      AND event.processedAt < :processedBefore
    """,
  )
  fun reclaimStaleProcessingEvents(
    @Param("expectedStatus") expectedStatus: ProcessedStatus,
    @Param("newStatus") newStatus: ProcessedStatus,
    @Param("processedBefore") processedBefore: Instant,
  ): Int
}
