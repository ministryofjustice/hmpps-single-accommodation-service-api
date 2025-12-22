package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import java.util.UUID

data class SuitableApplication(
  val id: UUID,
  val applicationStatus: String,
  val placementStatus: String?,
)
