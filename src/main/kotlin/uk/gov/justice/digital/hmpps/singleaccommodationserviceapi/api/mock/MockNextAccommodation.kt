package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.OffenderReleaseType
import java.time.LocalDate

fun getMockedNextAccommodation(crn: String) = when (crn) {
  mockCrns[0] -> AccommodationDetail(
    type = AccommodationType.NO_FIXED_ABODE,
    endDate = LocalDate.parse("1970-01-01"),
    subType = AccommodationSubType.RENTED,
    name = "ACCOMMODATION NAME A",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = LocalDate.parse("1970-01-01"),
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
    endDate = LocalDate.parse("1970-01-01"),
    subType = AccommodationSubType.LODGING,
    name = "ACCOMMODATION NAME B",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.BAIL,
    startDate = LocalDate.parse("1970-01-01"),
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
    endDate = LocalDate.parse("1970-01-01"),
    subType = null,
    name = "ACCOMMODATION NAME C",
    isSettled = false,
    offenderReleaseType = OffenderReleaseType.LICENCE,
    startDate = LocalDate.parse("1970-01-01"),
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
    endDate = LocalDate.parse("1970-01-01"),
    subType = AccommodationSubType.OWNED,
    name = "ACCOMMODATION NAME D",
    isSettled = true,
    offenderReleaseType = OffenderReleaseType.REMAND,
    startDate = LocalDate.parse("1970-01-01"),
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
