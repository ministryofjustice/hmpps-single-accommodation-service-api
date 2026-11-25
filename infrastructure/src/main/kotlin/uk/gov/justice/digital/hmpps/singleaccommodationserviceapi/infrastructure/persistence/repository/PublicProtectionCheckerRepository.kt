package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.PublicProtectionCheckerEntity
import java.util.UUID

interface PublicProtectionCheckerRepository : JpaRepository<PublicProtectionCheckerEntity, UUID> {
  fun findAllBySasCaseId(sasCaseId: UUID): List<PublicProtectionCheckerEntity>
  fun findFirstBySasCaseIdOrderByCreatedAtDesc(sasCaseId: UUID): PublicProtectionCheckerEntity?
}
