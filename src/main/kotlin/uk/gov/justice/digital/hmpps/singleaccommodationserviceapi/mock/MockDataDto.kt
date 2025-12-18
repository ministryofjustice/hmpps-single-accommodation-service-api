package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dtr.DtrDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Cas2EligibilityResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus

data class MockDataDto(
  val crns: Map<String, MockCrnDataDto>,
)

data class MockCrnDataDto(
  val crn: String,
  val dtrs: List<DtrDto>,
  val eligibility: MockEligibilityDto,
)

data class MockEligibilityDto(
  val caseStatus: CaseStatus,
  val caseActions: List<String>,
  val cas2: Cas2EligibilityResult,
  val cas3: ServiceResult,
)
