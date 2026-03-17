package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

interface CaseRepository : JpaRepository<CaseEntity, UUID> {

  @EntityGraph(attributePaths = ["caseId"])
  @Query(
    """
    SELECT c FROM CaseEntity c 
    JOIN CaseIdentifierEntity ci ON ci.caseEntity.id = c.id WHERE ci.identifier = :crn AND ci.identifierType = 'CRN'
    """,
  )
  fun findByCrn(crn: String): CaseEntity?

  @Query(
    """
    select distinct c
    from CaseEntity c
    join fetch c._caseIdentifiers ci
    where ci.identifier = :identifier
    and ci.identifierType = :identifierType
  """,
  )
  fun findByIdentifier(identifier: String, identifierType: IdentifierType): CaseEntity?

  @Query(
    """
    select distinct c
    from CaseEntity c
    join fetch c._caseIdentifiers ci
    where(
       (:crns is not null and ci.identifierType = 'CRN' and ci.identifier in :crns) or 
       (:prisonNumbers is not null and ci.identifierType = 'PRISON_NUMBER' and ci.identifier in :prisonNumbers)
    )
  """,
  )
  fun findByIdentifiers(prisonNumbers: List<String>?, crns: List<String>?): CaseEntity?

  /**
   * Basic projection, ensuring we bypass the hibernate cache and get the database data
   */
  @Query("SELECT c.tier FROM CaseEntity c JOIN CaseIdentifierEntity ci WHERE ci.identifier = :crn AND ci.identifierType = 'PRISON_NUMBER'")
  fun findTierScoreByCrn(@Param("crn") crn: String): TierScore?
}
