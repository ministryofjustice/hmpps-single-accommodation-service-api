package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CurrentAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.NextAccommodationDto

val mockAccommodationStatus = AccommodationStatus(
  CurrentAccommodationDto(type = AccommodationType.CAS1_MOCK, endDate = mockedLocalDate),
  NextAccommodationDto(type = AccommodationType.PRIVATE_ADDRESS_MOCK, startDate = mockedLocalDate),
)
