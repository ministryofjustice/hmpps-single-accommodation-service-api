package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.util.UUID

interface AccommodationTypeRepository :
  JpaRepository<AccommodationTypeEntity, UUID>,
  ReferenceDataRepository<AccommodationTypeEntity> {
  override fun findAllByActiveIsTrueOrderByName(): List<AccommodationTypeEntity>
}
