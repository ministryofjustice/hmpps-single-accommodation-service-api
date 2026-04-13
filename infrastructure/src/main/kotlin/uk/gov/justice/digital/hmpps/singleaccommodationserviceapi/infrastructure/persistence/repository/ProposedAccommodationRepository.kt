package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.util.UUID

@JaversSpringDataAuditable
interface ProposedAccommodationRepository : JpaRepository<ProposedAccommodationEntity, UUID> {
  @Query(
    """
    select pa from ProposedAccommodationEntity pa 
    join CaseIdentifierEntity ci on ci.caseEntity.id = pa.caseId 
    where pa.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrn(id: UUID, crn: String): ProposedAccommodationEntity?

  @Query(
    """
    select pa from ProposedAccommodationEntity pa 
    join CaseIdentifierEntity ci on ci.caseEntity.id = pa.caseId 
    where ci.identifier = :crn and ci.identifierType = 'CRN'
    order by pa.createdAt desc 
  """,
  )
  fun findAllByCrnOrderByCreatedAtDesc(crn: String): List<ProposedAccommodationEntity>

  @Query(
    """
    select pa from ProposedAccommodationEntity pa
    join CaseIdentifierEntity ci on ci.caseEntity.id = pa.caseId 
    join fetch pa.notes 
    where ci.identifier = :crn and ci.identifierType = 'CRN'
    order by pa.createdAt desc 
    """,
  )
  fun findAllWithNotesByCrnOrderByCreatedAtDesc(crn: String): List<ProposedAccommodationEntity>

  @Query(
    """
    select pa from ProposedAccommodationEntity pa
    join CaseIdentifierEntity ci on ci.caseEntity.id = pa.caseId
    left join fetch pa.notes 
    where pa.id = :id
    and ci.identifier = :crn and ci.identifierType = 'CRN'
    """,
  )
  fun findByIdAndCrnWithNotes(id: UUID, crn: String): ProposedAccommodationEntity?
}
