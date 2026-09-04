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

  @Modifying
  @Query(
    value = """
      DELETE FROM inbox_event
      WHERE id IN (
        SELECT id FROM inbox_event
        WHERE processed_status IN (:statuses)
          AND processed_at < :cutoff
        LIMIT :batchSize
      )
    """,
    nativeQuery = true,
  )
  fun deleteRetainedEvents(
    @Param("statuses") statuses: List<String>,
    @Param("cutoff") cutoff: Instant,
    @Param("batchSize") batchSize: Int,
  ): Int
}
