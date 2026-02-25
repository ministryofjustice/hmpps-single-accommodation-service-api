package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.util.UUID

interface ProposedAccommodationRepository : JpaRepository<ProposedAccommodationEntity, UUID> {
  fun findByCrn(crn: String): ProposedAccommodationEntity?
  fun findByIdAndCrn(id: UUID, crn: String): ProposedAccommodationEntity?
  fun findAllByCrnOrderByCreatedAtDesc(crn: String): List<ProposedAccommodationEntity>
}
