package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

data class Cas2EligibilityResult(
  val hdc: ServiceResult,
  val prisonBail: ServiceResult,
  val courtBail: ServiceResult,
)
