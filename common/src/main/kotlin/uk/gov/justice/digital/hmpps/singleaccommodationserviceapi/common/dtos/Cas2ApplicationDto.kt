package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class Cas2ApplicationDto(
  val uiUrl: String,
  val application: Cas2ApplicationSummaryDto,
)

data class Cas2ApplicationSummaryDto(
  val id: UUID,
  val status: String,
)
