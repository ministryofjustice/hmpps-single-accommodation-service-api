package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

fun getMockedCas3Eligibility(crn: String) = when (crn) {
  mockCrns[0] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start temporary accommodation referral in 2 days!!"),
  )
  mockCrns[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start temporary accommodation referral!!"),
  )
  mockCrns[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  mockCrns[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2HdcEligibility(crn: String) = when (crn) {
  mockCrns[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start HDC referral!!"),
  )
  mockCrns[1] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start HDC referral in 20 days!!"),
  )
  mockCrns[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  mockCrns[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2PrisonBailEligibility(crn: String) = when (crn) {
  mockCrns[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = listOf("Action 1!!"),
  )
  mockCrns[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  mockCrns[2] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start prison bail referral!!"),
  )
  mockCrns[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2CourtBailEligibility(crn: String) = when (crn) {
  mockCrns[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = listOf("Action 1!!"),
  )
  mockCrns[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  mockCrns[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  mockCrns[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start court bail referral!!"),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
