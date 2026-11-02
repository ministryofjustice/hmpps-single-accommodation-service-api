package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew

/**
 * Evaluation context that carries state through decision tree traversal.
 * Contains the domain data and the current best-known ServiceResult.
 *
 */
data class EvaluationContext(
  val data: DomainData,
  /**
   * Will be `null` until a [ContextUpdater] sets a concrete status
   */
  val currentResult: ServiceResultNew?,
  /**
   * Failure reasons from the most recent failing RuleSet without a fail [ContextUpdater].
   * Kept here so they survive even while [currentResult] is still `null`.
   */
  val failureReasons: List<FailureReason> = emptyList(),
) {
  /** Passes the context through unchanged, apart from recording the failing RuleSet's failure reasons. */
  fun withFailureReasons(reasons: List<FailureReason>) = copy(
    currentResult = currentResult?.copy(failureReasons = reasons),
    failureReasons = reasons,
  )
}
