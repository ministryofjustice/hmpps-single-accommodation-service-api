package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.util.UUID

interface InboxEventRepository : JpaRepository<InboxEventEntity, UUID> {
  fun findAllByProcessedStatus(processedStatus: ProcessedStatus, pageable: Pageable): List<InboxEventEntity>

  fun findByIdAndProcessedStatusIs(eventId: UUID, processedStatus: ProcessedStatus): InboxEventEntity?

  @Modifying
  @Query(
    """
    update InboxEventEntity iee
    set iee.processedStatus = :#{T(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus).PENDING},
        iee.processedAt = null
    where iee.processedStatus = :#{T(uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus).FAILED}
    and iee.id in :ids
    """,
  )
  fun setAllFailedToPending(ids: Set<UUID>): Int

  @Query(
    """
    select iee.id from InboxEventEntity iee where iee.processedStatus = :processedStatus 
  """,
  )
  fun findAllIdsByProcessedStatus(processedStatus: ProcessedStatus): Set<UUID>
}
