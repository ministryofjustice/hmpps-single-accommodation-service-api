package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus

fun getMockedCas3Eligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    action =
    RuleAction("Start temporary accommodation referral in 2 days!!", true),
  )

  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    action = RuleAction("Start temporary accommodation referral!!"),
  )

  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  else -> null
}

fun getMockedCas2HdcEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    action = RuleAction("Start HDC referral!!"),
  )

  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    action = RuleAction("Start HDC referral in 20 days!!", true),
  )

  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  else -> null
}

fun getMockedCas2PrisonBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    suitableApplicationId = null,
    action = RuleAction("Start prison bail referral!!"),
  )

  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  else -> null
}

fun getMockedCas2CourtBailEligibility(availableCrnList: List<String>, crn: String) = when (crn) {
  availableCrnList[0] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[1] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[2] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    suitableApplicationId = null,
  )

  availableCrnList[3] -> ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    suitableApplicationId = null,
    action = RuleAction("Start court bail referral!!"),
  )

  else -> null
}
