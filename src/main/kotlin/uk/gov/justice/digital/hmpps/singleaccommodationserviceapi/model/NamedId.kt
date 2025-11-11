package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model

import java.util.UUID

data class NamedId(
  val id: UUID,
  val name: String,
  val code: String? = null,
)
