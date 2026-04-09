package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

interface CaseRepository : JpaRepository<CaseEntity, UUID> {

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where ci.identifier = :identifier
    and ci.identifierType = :identifierType
  """,
  )
  fun findByIdentifier(identifier: String, identifierType: IdentifierType): CaseEntity?

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where ((:crns is not null and ci.identifierType = 'CRN' and ci.identifier in :crns)
       or (:prisonNumbers is not null and ci.identifierType = 'PRISON_NUMBER' and ci.identifier in :prisonNumbers))
  """,
  )
  fun findByIdentifiers(prisonNumbers: List<String>?, crns: List<String>?): CaseEntity?

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where ci.identifierType = 'CRN' and ci.identifier in :crns
  """,
  )
  fun findByCrns(crns: List<String>): List<CaseEntity>

  @Query(
    """
SELECT identifier
FROM unnest((:crns)::text[]) AS identifier
WHERE identifier NOT IN (
  SELECT identifier 
  FROM sas_case_identifier 
  WHERE identifier_type = 'CRN'
);
  """,
    nativeQuery = true,
  )
  fun findMissingCrns(crns: Array<String>): List<String>

  fun findByCrn(crn: String) = findByIdentifier(crn, IdentifierType.CRN)
  fun findByPrisonNumber(prisonNumber: String) = findByIdentifier(prisonNumber, IdentifierType.PRISON_NUMBER)
}
