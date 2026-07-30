package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.util.UUID

interface AccommodationTypeRepository :
  JpaRepository<AccommodationTypeEntity, UUID>,
  ReferenceDataRepository<AccommodationTypeEntity> {
  override fun findAllByActiveIsTrueOrderByName(): List<AccommodationTypeEntity>
  fun findAllByActiveIsTrueAndIsProposedIsTrueOrderByName(): List<AccommodationTypeEntity>
  fun findByCodeAndActiveIsTrue(code: String): AccommodationTypeEntity?
  fun findByCode(code: String): AccommodationTypeEntity?
  fun findAllBySettledTypeAndActiveIsTrue(settledType: AccommodationSettledType): List<AccommodationTypeEntity>
  fun findAllByIsHomelessIsTrueAndActiveIsTrue(): List<AccommodationTypeEntity>
}
