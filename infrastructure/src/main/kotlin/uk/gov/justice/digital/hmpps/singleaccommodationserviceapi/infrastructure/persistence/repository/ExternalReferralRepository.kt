package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralStatus
import java.util.UUID

@JaversSpringDataAuditable
interface ExternalReferralRepository : JpaRepository<ExternalReferralEntity, UUID> {
  fun findByCaseId(caseId: UUID): ExternalReferralEntity?

  @Query(
    """
    select er from ExternalReferralEntity er
    join CaseIdentifierEntity ci on ci.caseEntity.id = er.caseId
    where  er.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrn(id: UUID, crn: String): ExternalReferralEntity?

  @Query(
    """
    select er from ExternalReferralEntity er
    left join fetch er.notes
    join CaseIdentifierEntity ci on ci.caseEntity.id = er.caseId
    where er.id = :id and ci.identifier = :crn and ci.identifierType = 'CRN'
  """,
  )
  fun findByIdAndCrnWithNotes(id: UUID, crn: String): ExternalReferralEntity?

  @Query(
    """
    select er from ExternalReferralEntity er
    join CaseIdentifierEntity ci on ci.caseEntity.id = er.caseId
    where ci.identifier = :crn and ci.identifierType = 'CRN'
    and (:statuses is null or er.status in :statuses)
    order by er.submissionDate desc
  """,
  )
  fun searchByCrn(crn: String, statuses: List<ExternalReferralStatus>?): List<ExternalReferralEntity>
}
