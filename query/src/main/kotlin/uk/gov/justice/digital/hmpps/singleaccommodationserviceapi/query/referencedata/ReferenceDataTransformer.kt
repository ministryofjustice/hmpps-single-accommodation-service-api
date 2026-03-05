package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ReferenceData

object ReferenceDataTransformer {

  fun toReferenceDataDto(entity: ReferenceData) = ReferenceDataDto(
    id = entity.id,
    name = entity.name,
  )
}
