package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dutytorefer.DutyToReferDto

val mockCrns = listOf(
  "X371199",
  "X968879",
  "X966926",
  "X969031",
)

fun getMockedData() = MockData(
  crns = mockCrns.associate {
    it to MockCrnData(
      dutyToRefers = getMockedDutyToRefers(it),
      crn = it,
    )
  },
)

data class MockData(
  val crns: Map<String, MockCrnData>,
)

data class MockCrnData(
  val crn: String,
  val dutyToRefers: List<DutyToReferDto>,
)
