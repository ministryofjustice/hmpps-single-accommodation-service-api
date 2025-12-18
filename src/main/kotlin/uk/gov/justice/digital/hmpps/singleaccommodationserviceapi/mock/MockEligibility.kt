package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Cas2EligibilityResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

fun getMockedEligibility(): MockEligibilityDto = MockEligibilityDto(
  caseStatus = CaseStatus.ACTION_NEEDED,
  caseActions = listOf("Action 1!!"),
  cas2 = Cas2EligibilityResult(
    hdc = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplication = null,
      actions = emptyList(),
    ),
    prisonBail = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplication = null,
      actions = listOf("Action 1!!"),
    ),
    courtBail = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      suitableApplication = null,
      actions = listOf("Action 1!!"),
    ),
  ),
  cas3 = ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  ),
)
