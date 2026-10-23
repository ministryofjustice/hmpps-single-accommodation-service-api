package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ReferenceData

interface ReferenceDataRepository<T : ReferenceData> {
  fun findAllByActiveIsTrueOrderByName(sort: Sort): List<T>
  fun findAllByActiveIsTrueOrderByName(): List<T> = findAllByActiveIsTrueOrderByName(Sort.by(Sort.Order.asc("name").ignoreCase()))
}
