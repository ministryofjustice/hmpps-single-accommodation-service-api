package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PrivateAddressesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

fun getMockedData() = MockData(
  crns = mockCrns.associate {
    it to MockCrnData(
      dutyToRefers = getMockedDutyToRefers(it),
      crn = it,
      photoUrl = mockPhotoUrl,
      currentAccommodation = getMockedCurrentAccommodation(it),
      nextAccommodation = getMockedNextAccommodation(it),
      accommodation = getMockedAccommodation(it),
      cas3Eligibility = getMockedCas3Eligibility(it),
      cas2HdcEligibility = getMockedCas2HdcEligibility(it),
      cas2PrisonBailEligibility = getMockedCas2PrisonBailEligibility(it),
      cas2CourtBailEligibility = getMockedCas2CourtBailEligibility(it),
      privateAddresses = getMockedPrivateAddresses(it),
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
  val cas2HdcEligibility: ServiceResult,
  val cas2PrisonBailEligibility: ServiceResult,
  val cas2CourtBailEligibility: ServiceResult,
  val cas3Eligibility: ServiceResult,
  val privateAddresses: PrivateAddressesDto,
)
