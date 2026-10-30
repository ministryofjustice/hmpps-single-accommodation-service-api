package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

interface CaseRepository : JpaRepository<CaseEntity, UUID> {

  @Query(
    """
    select ci.identifier
    from CaseEntity c
    join c.caseIdentifiers ci
    where ci.identifierType = 'CRN'
      and c.archived = false
  """,
  )
  fun findAllCrns(): List<String>

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where ci.identifier = :identifier
    and ci.identifierType = :identifierType
    and c.archived = false
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
      and c.archived = false
  """,
  )
  fun findByIdentifiers(crns: List<String>?, prisonNumbers: List<String>?): CaseEntity?

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where ci.identifierType = 'CRN' and ci.identifier in :crns
      and c.archived = false
  """,
  )
  fun findByCrns(crns: List<String>): List<CaseEntity>

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  fun findWithIdentifiersById(id: UUID): CaseEntity?

  @EntityGraph(attributePaths = ["caseIdentifiers"])
  @Query(
    """
    select distinct c
    from CaseEntity c
    join c.caseIdentifiers ci
    where c.id in :ids
      and ci.identifierType = 'CRN'
      and c.archived = false
  """,
  )
  fun findAllWithIdentifiersByIdIn(ids: Collection<UUID>): List<CaseEntity>

  @Query(
    """
SELECT i.identifier
FROM unnest((:crns)::text[]) AS i(identifier)
WHERE NOT EXISTS (
  SELECT 1
  FROM sas_case_identifier sci
  WHERE sci.identifier = i.identifier
    AND sci.identifier_type = 'CRN'
)
  """,
    nativeQuery = true,
  )
  fun findUnpersistedCrns(crns: Array<String>): List<String>

  // this query ignores the archived status for SAR
  @Query(
    nativeQuery = true,
    value = """
    SELECT DISTINCT c.*
    FROM sas_case c
    JOIN sas_case_identifier sci ON sci.case_id = c.id
    WHERE (sci.identifier_type = 'CRN' AND sci.identifier = :crn)
      OR (sci.identifier_type = 'PRISON_NUMBER' AND sci.identifier = :prisonNumber)
    """,
  )
  fun findCaseForSubjectAccessRequest(crn: String?, prisonNumber: String?): CaseEntity?

  fun findByCrn(crn: String) = findByIdentifier(crn, IdentifierType.CRN)
  fun findByPrisonNumber(prisonNumber: String) = findByIdentifier(prisonNumber, IdentifierType.PRISON_NUMBER)

  /**
   * Archives the case in a single atomic statement so it cannot be overwritten by a stale full-row update.
   * `versioned` bumps the `version` column, so any concurrent in-flight writer that loaded the case earlier
   * fails its optimistic lock check instead of writing `archived = false` back.
   * Returns the number of cases archived (0 if the case is unknown or already archived).
   */
  @Transactional
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("update versioned CaseEntity c set c.archived = true where c.id = :id and c.archived = false")
  fun archiveById(id: UUID): Int

  fun mapByCrns(crns: List<String>) = findByCrns(crns)
    .flatMap { entity ->
      entity.caseIdentifiers
        .filter { it.identifierType == IdentifierType.CRN && it.identifier in crns }
        .map { it.identifier to entity }
    }
    .toMap()
}
