package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mocks.MockCase

data class MockCasesResponse(
  val cases: List<MockCase>,
)

data class CasesResponse(
  val cases: List<Case>,
)
