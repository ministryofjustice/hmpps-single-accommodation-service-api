package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dutytorefer.DutyToReferDto

val mockCrns = listOf(
  "X371199",
  "X968879",
  "X966926",
  "X969031",
)

val mockPhotoUrl: String =
  "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg"

fun getMockedData() = MockData(
  crns = mockCrns.associate {
    it to MockCrnData(
      dutyToRefers = getMockedDutyToRefers(it),
      crn = it,
      photoUrl = mockPhotoUrl,
      currentAccommodation = getMockedCurrentAccommodation(it),
      nextAccommodation = getMockedNextAccommodation(it),
      accommodation = getMockedAccommodation(it),
    )
  },
)

data class MockData(
  val crns: Map<String, MockCrnData>,
)

data class MockCrnData(
  val crn: String,
  val dutyToRefers: List<DutyToReferDto>,
  val currentAccommodation: AccommodationDetail,
  val nextAccommodation: AccommodationDetail,
  val photoUrl: String,
  val accommodation: AccommodationDto,
)
