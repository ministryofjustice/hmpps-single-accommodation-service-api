package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetailDto

data class AccommodationResponse(
  val crn: String,
  val current: AccommodationDetailDto,
  val next: AccommodationDetailDto,
)
