package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason

data class RuleSetResult(
  val status: RuleSetStatus,
  val failureReasons: List<FailureReason>,
)

enum class RuleSetStatus {
  PASS,
  FAIL,
}
