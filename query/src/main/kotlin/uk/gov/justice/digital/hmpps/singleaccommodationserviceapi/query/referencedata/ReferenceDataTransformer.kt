package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto

object ReferenceDataTransformer {

  fun toReferenceDataDto(entity: ReferenceData) = ReferenceDataDto(
    id = entity.id,
    name = entity.name,
    description = entity.description,
  )
}
