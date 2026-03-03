package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityAreaDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity

object LocalAuthorityAreaTransformer {

  fun toLocalAuthorityAreaDto(entity: LocalAuthorityAreaEntity) = LocalAuthorityAreaDto(
    id = entity.id,
    identifier = entity.identifier,
    name = entity.name,
    active = entity.active,
  )
}
