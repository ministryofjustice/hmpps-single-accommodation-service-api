package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import java.time.Instant
import java.util.UUID

@JaversSpringDataAuditable
interface OtherAccommodationReferralRepository : JpaRepository<OtherAccommodationReferralEntity, UUID> {

  fun findByCaseId(caseId: UUID): OtherAccommodationReferralEntity?

  @Query(
    """
    select d from OtherAccommodationReferralEntity d 
    join CaseIdentifierEntity ci on ci.caseEntity.id = d.caseId
    where  d.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrn(id: UUID, crn: String): OtherAccommodationReferralEntity?
  fun findFirstByCaseIdOrderByCreatedAtDesc(caseId: UUID): OtherAccommodationReferralEntity?
  fun findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId: UUID, status: List<OorStatus>): List<OtherAccommodationReferralEntity>

  @Query(
    """
    select oor from OtherAccommodationReferralEntity oor
    left join fetch oor.notes
    join CaseIdentifierEntity ci on ci.caseEntity.id = oor.caseId
    where oor.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrnWithNotes(id: UUID, crn: String): OtherAccommodationReferralEntity?

  @Query(
    """
    select oor from OtherAccommodationReferralEntity oor
    where oor.caseId = :caseId
    and oor.createdAt >= :startDate
    and oor.createdAt <= :endDate
    order by oor.createdAt desc
  """,
  )
  fun findAllForSar(caseId: UUID, startDate: Instant, endDate: Instant): List<OtherAccommodationReferralEntity>
}
