package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class ReferenceDataDto(
  val id: UUID,
  val name: String?,
)
