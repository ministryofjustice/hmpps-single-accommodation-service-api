package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import java.util.UUID

interface AccommodationStatusRepository :
  JpaRepository<AccommodationStatusEntity, UUID>,
  ReferenceDataRepository<AccommodationStatusEntity> {
  override fun findAllByActiveIsTrueOrderByName(): List<AccommodationStatusEntity>
}
