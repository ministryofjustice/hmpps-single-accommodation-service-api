package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

/**
 * Interface for updating evaluation context based on RuleSet execution results.
 * This separates analysis/mapping logic from tree traversal.
 */
interface ContextUpdater {
  /**
   * This is where mapping from RuleSetResult to ServiceResult happens.
   */
  fun update(context: EvaluationContext): EvaluationContext
}
