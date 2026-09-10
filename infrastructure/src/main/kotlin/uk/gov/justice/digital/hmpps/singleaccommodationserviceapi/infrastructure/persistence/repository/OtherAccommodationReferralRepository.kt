package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import java.util.UUID

interface OtherAccommodationReferralRepository : JpaRepository<OtherAccommodationReferralEntity, UUID> {
  fun findByCaseId(caseId: UUID): OtherAccommodationReferralEntity?

  @Query(
    """
    select oar from OtherAccommodationReferralEntity oar
    join CaseIdentifierEntity ci on ci.caseEntity.id = oar.caseId
    where  oar.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrn(id: UUID, crn: String): OtherAccommodationReferralEntity?

  @Query(
    """
    select oar from OtherAccommodationReferralEntity oar
    left join fetch oar.notes
    join CaseIdentifierEntity ci on ci.caseEntity.id = oar.caseId
    where oar.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrnWithNotes(id: UUID, crn: String): OtherAccommodationReferralEntity?
}
