package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult

data class Cas1EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
)
