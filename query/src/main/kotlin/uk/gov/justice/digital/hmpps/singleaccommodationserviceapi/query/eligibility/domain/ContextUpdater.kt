package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

abstract class ContextUpdater {
  fun update(context: EvaluationContext, failureReasons: List<FailureReason> = emptyList()): EvaluationContext {
    val updatedServiceResult = toServiceResult(context)
    // Use reasons from the Ruleset that just failed, fallback to any updater defined by the context
    val mergedFailureReasons = failureReasons.ifEmpty { updatedServiceResult.failureReasons }

    return context.copy(
      currentResult = updatedServiceResult.copy(failureReasons = mergedFailureReasons),
    )
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResult

  companion object {
    /** Returns a ContextUpdater that replaces the current ServiceResult with [result], ignoring the context. */
    fun constant(result: ServiceResult): ContextUpdater = object : ContextUpdater() {
      override fun toServiceResult(context: EvaluationContext): ServiceResult = result
    }

    /** Returns a ContextUpdater that leaves the current ServiceResult unchanged. */
    fun identity(): ContextUpdater = object : ContextUpdater() {
      override fun toServiceResult(context: EvaluationContext): ServiceResult = context.currentResult
    }
  }
}
