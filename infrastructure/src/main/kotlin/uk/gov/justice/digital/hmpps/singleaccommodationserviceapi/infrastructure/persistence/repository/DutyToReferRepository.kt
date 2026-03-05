package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import java.util.UUID

interface DutyToReferRepository : JpaRepository<DutyToReferEntity, UUID> {
  fun findByCrn(crn: String): DutyToReferEntity?
}
