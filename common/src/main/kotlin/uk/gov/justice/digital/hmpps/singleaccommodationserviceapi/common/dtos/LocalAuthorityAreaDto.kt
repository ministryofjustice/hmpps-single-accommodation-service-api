package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class LocalAuthorityAreaDto(
  val id: UUID,
  val identifier: String,
  val name: String,
  val active: Boolean,
)
