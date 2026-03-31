package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import java.util.UUID

interface DutyToReferRepository : JpaRepository<DutyToReferEntity, UUID> {

  fun findByCaseId(caseId: UUID): DutyToReferEntity?

  @Query(
    """
    select d from DutyToReferEntity d 
    join CaseIdentifierEntity ci on ci.caseEntity.id = d.caseId
    where  d.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrn(id: UUID, crn: String): DutyToReferEntity?
  fun findFirstByCaseIdOrderByCreatedAtDesc(caseId: UUID): DutyToReferEntity?
}
