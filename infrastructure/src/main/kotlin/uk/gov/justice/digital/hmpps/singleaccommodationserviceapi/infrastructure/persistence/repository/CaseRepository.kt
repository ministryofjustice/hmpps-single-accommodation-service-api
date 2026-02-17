package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.util.UUID

interface CaseRepository : JpaRepository<CaseEntity, UUID> {

  fun findByCrn(crn: String): CaseEntity?

  /**
   * Basic projection, ensuring we bypass the hibernate cache and get the database data
   */
  @Query("SELECT c.tier FROM CaseEntity c WHERE c.crn = :crn")
  fun findTierScoreByCrn(@Param("crn") crn: String): TierScore?
}