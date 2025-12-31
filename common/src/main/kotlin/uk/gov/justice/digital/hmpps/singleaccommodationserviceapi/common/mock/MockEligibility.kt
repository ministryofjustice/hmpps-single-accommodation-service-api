package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus


fun getMockedCaseActions() = listOf("Action 1", "Action 2", "Action 2", "Action 3")

val mockedCaseStatus = CaseStatus.ACTION_NEEDED

fun getMockedCas3Eligibility() = ServiceResult(
  serviceStatus = ServiceStatus.NOT_ELIGIBLE,
  suitableApplication = null,
  actions = emptyList(),
)

fun getMockedCas2HdcEligibility() = ServiceResult(
  serviceStatus = ServiceStatus.NOT_ELIGIBLE,
  suitableApplication = null,
  actions = emptyList(),
)

fun getMockedCas2PrisonBailEligibility() = ServiceResult(
  serviceStatus = ServiceStatus.NOT_STARTED,
  suitableApplication = null,
  actions = listOf("Action 1!!"),
)

fun getMockedCas2CourtBailEligibility() = ServiceResult(
  serviceStatus = ServiceStatus.UPCOMING,
  suitableApplication = null,
  actions = listOf("Action 1!!"),
)
