package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus

fun getMockedCas3Eligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    actions = listOf(
      RuleAction("Start temporary accommodation referral in 2 days!!", true)
    ),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    actions = listOf(RuleAction("Start temporary accommodation referral!!")),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2HdcEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    actions = listOf(RuleAction("Start HDC referral!!")),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    actions = listOf(RuleAction("Start HDC referral in 20 days!!", true)),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2PrisonBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    actions = listOf(RuleAction("Start prison bail referral!!")),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}

fun getMockedCas2CourtBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
    actions = emptyList(),
  )
  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    actions = listOf(RuleAction("Start court bail referral!!")),
  )
  else -> error("Unallowed CRN $crn - ensure mock data is appropriate")
}
