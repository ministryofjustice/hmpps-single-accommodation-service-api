package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import java.util.UUID

interface LocalAuthorityAreaRepository : JpaRepository<LocalAuthorityAreaEntity, UUID> {
  fun findAllByNameContainingIgnoreCaseAndActiveOrderByName(search: String, active: Boolean): List<LocalAuthorityAreaEntity>
  fun findAllByActiveOrderByName(active: Boolean): List<LocalAuthorityAreaEntity>
}
