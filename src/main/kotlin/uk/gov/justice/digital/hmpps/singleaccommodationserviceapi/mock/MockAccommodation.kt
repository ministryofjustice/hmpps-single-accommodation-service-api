package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto

fun getMockedAccommodation(crn: String) = AccommodationDto(
  crn = crn,
  current = getMockedCurrentAccommodation(crn),
  next = getMockedNextAccommodation(crn),
)
