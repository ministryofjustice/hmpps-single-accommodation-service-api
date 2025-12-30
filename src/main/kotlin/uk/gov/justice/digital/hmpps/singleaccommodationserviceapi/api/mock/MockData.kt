package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dutytorefer.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress.PrivateAddressesDto

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
      cas3Eligibility = getMockedCas3Eligibility(),
      cas2HdcEligibility = getMockedCas2HdcEligibility(),
      cas2PrisonBailEligibility = getMockedCas2PrisonBailEligibility(),
      cas2CourtBailEligibility = getMockedCas2CourtBailEligibility(),
      caseActions = getMockedCaseActions(),
      caseStatus = mockedCaseStatus,
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
  val caseActions: List<String>,
  val caseStatus: CaseStatus,
  val privateAddresses: PrivateAddressesDto,
)
