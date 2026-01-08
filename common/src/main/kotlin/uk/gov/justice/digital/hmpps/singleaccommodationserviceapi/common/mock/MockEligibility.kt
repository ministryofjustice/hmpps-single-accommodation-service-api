package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus

fun getMockedCas3Eligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start temporary accommodation referral in 2 days!!"),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start temporary accommodation referral!!"),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2HdcEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start HDC referral!!"),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start HDC referral in 20 days!!"),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2PrisonBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplication = null,
    actions = listOf("Start prison bail referral!!"),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2CourtBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplication = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplication = null,
    actions = listOf("Start court bail referral!!"),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
