package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetails

data class AccommodationResponse(
  val crn: String,
  val current: AccommodationDetails,
  val next: AccommodationDetails,
)
