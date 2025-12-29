package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockedLocalDate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildAccommodationDetails(
  accommodationType: AccommodationType = AccommodationType.CAS1,
  accommodationSubType: AccommodationSubType? = AccommodationSubType.OWNED,
  name: String? = "The CAS1 House",
  isSettled: Boolean? = true,
  offenderReleaseType: OffenderReleaseType? = OffenderReleaseType.LICENCE,
  startDate: LocalDate? = mockedLocalDate,
  endDate: LocalDate? = mockedLocalDate,
  address: AddressDetails? = buildAddress(),
) = AccommodationDetail(
  type = accommodationType,
  subType = accommodationSubType,
  name = name,
  isSettled = isSettled,
  offenderReleaseType = offenderReleaseType,
  startDate = startDate,
  endDate = endDate,
  address = address,
)

fun buildNoFixedAbodeAccommodationDetails() = buildAccommodationDetails(
  accommodationType = AccommodationType.NO_FIXED_ABODE,
  accommodationSubType = null,
  name = null,
  isSettled = null,
  offenderReleaseType = null,
  startDate = null,
  endDate = null,
  address = null,
)

fun buildCas1Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  placementStatus: Cas1PlacementStatus? = null,
) = Cas1Application(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
)

fun buildAccommodationResponse(
  crn: String = "!!FAKECRN",
  currentAccommodationDetail: AccommodationDetail = buildAccommodationDetails(),
  nextAccommodationDetail: AccommodationDetail = buildNoFixedAbodeAccommodationDetails(),
) = AccommodationDto(crn = crn, currentAccommodationDetail, nextAccommodationDetail)

fun buildAddress(
  line1: String = "!!Line 1",
  line2: String? = "!!Line 2",
  region: String? = "!!REGION",
  city: String = "!!CITY",
  postcode: String = "!!POSTCODE",
) = AddressDetails(
  line1 = line1,
  line2 = line2,
  region = region,
  city = city,
  postcode = postcode,
)
