package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dtr.DtrDto

data class MockDataDto(
  val crns: Map<String, MockCrnDataDto>,
)

data class MockCrnDataDto(
  val crn: String,
  val dtrs: List<DtrDto>,
)
