package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.util.UUID

interface CaseRepository : JpaRepository<CaseEntity, UUID> {

  fun findByCrn(crn: String): CaseEntity?
}