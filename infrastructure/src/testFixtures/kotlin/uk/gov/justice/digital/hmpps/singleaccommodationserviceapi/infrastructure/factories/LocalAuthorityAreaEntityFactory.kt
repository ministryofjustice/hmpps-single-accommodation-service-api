package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import java.util.UUID

fun buildLocalAuthorityAreaEntity(
  id: UUID = UUID.randomUUID(),
  code: String = "E09000001",
  name: String = "City of London",
  active: Boolean = true,
) = LocalAuthorityAreaEntity(
  id = id,
  code = code,
  name = name,
  active = active,
)
