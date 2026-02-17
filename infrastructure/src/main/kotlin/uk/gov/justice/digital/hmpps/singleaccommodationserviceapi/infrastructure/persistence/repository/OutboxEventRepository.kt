package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEventEntity, UUID> {
  fun findAllByProcessedStatus(processedStatus: ProcessedStatus): List<OutboxEventEntity>
  fun findTopByOrderByCreatedAtDesc(): OutboxEventEntity?

}
