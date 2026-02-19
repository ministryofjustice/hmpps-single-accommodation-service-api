package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import java.util.UUID

interface InboxEventRepository : JpaRepository<InboxEventEntity, UUID> {
  fun findAllByProcessedStatus(processedStatus: ProcessedStatus): List<InboxEventEntity>
}
