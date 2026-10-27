package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ReferenceData

interface ReferenceDataRepository<T : ReferenceData> {
  fun findAllByActiveIsTrue(sort: Sort): List<T>
  fun findAllByActiveIsTrueOrderByName(): List<T> = findAllByActiveIsTrue(Sort.by(Sort.Order.asc("name").ignoreCase()))
}
