package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType

fun getMockedAccommodation(crn: String) = AccommodationDto(
  crn = crn,
  current = getMockedCurrentAccommodation(crn),
  next = getMockedNextAccommodation(crn),
)

fun getMockedNextAccommodation(crn: String) = when (crn) {
  mockCrns[0] -> AccommodationDetail(
    type = AccommodationType.NO_FIXED_ABODE,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.RENTED,
    name = "ACCOMMODATION NAME A",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town A",
      region = "Region A",
      city = "City A",
      postcode = "AB12 3CD",
    ),
  )

  mockCrns[1] -> AccommodationDetail(
    type = AccommodationType.CAS2,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.LODGING,
    name = "ACCOMMODATION NAME B",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.BAIL,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town B",
      region = "Region B",
      city = "City B",
      postcode = "AB12 3CD",
    ),
  )

  mockCrns[2] -> AccommodationDetail(
    type = AccommodationType.PRISON,
    endDate = mockedLocalDate,
    subType = null,
    name = "ACCOMMODATION NAME C",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town C",
      region = "Region C",
      city = "City C",
      postcode = "AB12 3CD",
    ),
  )

  mockCrns[3] -> AccommodationDetail(
    type = AccommodationType.PRIVATE,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.OWNED,
    name = "ACCOMMODATION NAME D",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.REMAND,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town D",
      region = "Region D",
      city = "City D",
      postcode = "AB12 3CD",
    ),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCurrentAccommodation(crn: String) = when (crn) {
  mockCrns[0] -> AccommodationDetail(
    type = AccommodationType.NO_FIXED_ABODE,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.RENTED,
    name = "ACCOMMODATION NAME A",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town A",
      region = "Region A",
      city = "City A",
      postcode = "AB12 3CD",
    ),
  )
  mockCrns[1] -> AccommodationDetail(
    type = AccommodationType.CAS2,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.LODGING,
    name = "ACCOMMODATION NAME B",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.BAIL,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town B",
      region = "Region B",
      city = "City B",
      postcode = "AB12 3CD",
    ),
  )
  mockCrns[2] -> AccommodationDetail(
    type = AccommodationType.PRISON,
    endDate = mockedLocalDate,
    subType = null,
    name = "ACCOMMODATION NAME C",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town C",
      region = "Region C",
      city = "City C",
      postcode = "AB12 3CD",
    ),
  )
  mockCrns[3] -> AccommodationDetail(
    type = AccommodationType.PRIVATE,
    endDate = mockedLocalDate,
    subType = AccommodationSubType.OWNED,
    name = "ACCOMMODATION NAME D",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.REMAND,
    startDate = mockedLocalDate,
    address = AddressDetails(
      line1 = "123 Main Street",
      line2 = "Town D",
      region = "Region D",
      city = "City D",
      postcode = "AB12 3CD",
    ),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
