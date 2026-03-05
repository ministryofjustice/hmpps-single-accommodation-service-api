package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ReferenceData

interface ReferenceDataRepository<T : ReferenceData> {
  fun findAllByActiveIsTrueOrderByName(): List<T>
}
