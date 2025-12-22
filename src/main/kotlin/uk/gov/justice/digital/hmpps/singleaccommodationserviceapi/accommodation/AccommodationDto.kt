package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetail

data class AccommodationDto(
  val crn: String,
  val current: AccommodationDetail,
  val next: AccommodationDetail,
)
