package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason

interface RuleSet {
  /**
   * Human-readable label
   */
  val name: String

  fun getRules(): List<PrioritisedRule>

  fun failureReasons(): List<FailureReason> = getRules().mapNotNull { it.rule.failureReason }
}
