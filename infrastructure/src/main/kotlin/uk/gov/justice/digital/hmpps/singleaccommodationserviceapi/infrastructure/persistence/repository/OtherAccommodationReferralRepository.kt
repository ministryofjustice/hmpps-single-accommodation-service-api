package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus
import java.util.UUID

@JaversSpringDataAuditable
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

  @Query(
    """
    select oar from OtherAccommodationReferralEntity oar
    join CaseIdentifierEntity ci on ci.caseEntity.id = oar.caseId
    where ci.identifier = :crn and ci.identifierType = 'CRN'
    and (:statuses is null or oar.status in :statuses)
    order by oar.submissionDate desc
  """,
  )
  fun searchByCrn(crn: String, statuses: List<OtherAccommodationReferralStatus>?): List<OtherAccommodationReferralEntity>
}
