package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import java.util.UUID

interface OtherAccommodationReferralRepository : JpaRepository<OtherAccommodationReferralEntity, UUID> {
  fun findByCaseId(caseId: UUID): OtherAccommodationReferralEntity?
}
