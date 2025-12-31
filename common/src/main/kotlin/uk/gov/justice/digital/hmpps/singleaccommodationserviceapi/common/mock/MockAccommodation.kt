package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto

fun getMockedAccommodation(crn: String) = AccommodationDto(
  crn = crn,
  current = getMockedCurrentAccommodation(crn),
  next = getMockedNextAccommodation(crn),
)
