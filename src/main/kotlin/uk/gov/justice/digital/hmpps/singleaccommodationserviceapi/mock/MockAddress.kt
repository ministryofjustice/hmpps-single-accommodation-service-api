package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.OffenderReleaseType
import java.time.LocalDate

val mockAddress = AddressDetails(
  "!!val line1: String,",
  "!!val line2: String?,",
  "!!val region: String?,",
  "!!val city: String,",
  "!!val postCode: String,",
)

val mockCurrentAccommodationDetail = AccommodationDetail(
  type = AccommodationType.PRISON,
  subType = AccommodationSubType.RENTED,
  name = "!!TODO()",
  isSettled = true,
  offenderReleaseType = OffenderReleaseType.BAIL,
  startDate = LocalDate.now(),
  endDate = LocalDate.now().plusDays(10),
  address = mockAddress,
)

val mockNextAccommodationDetail = AccommodationDetail(
  type = AccommodationType.NO_FIXED_ABODE,
  subType = null,
  name = null,
  isSettled = null,
  offenderReleaseType = null,
  startDate = null,
  endDate = null,
  address = null,
)

fun mockAccommodationResponse(crn: String) = AccommodationDto(
  crn = "!!$crn",
  current = mockCurrentAccommodationDetail,
  next = mockNextAccommodationDetail,
)
