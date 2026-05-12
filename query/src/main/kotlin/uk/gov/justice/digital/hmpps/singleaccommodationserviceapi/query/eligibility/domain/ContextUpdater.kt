package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

abstract class ContextUpdater {
  open val propagatesFailureReasons: Boolean = false

  fun update(context: EvaluationContext, failureReasons: List<FailureReason> = emptyList()): EvaluationContext {
    val updatedServiceResult = toServiceResult(context)
    val reasonsToApply = if (propagatesFailureReasons) failureReasons else emptyList()

    return context.copy(
      currentResult = updatedServiceResult.copy(failureReasons = reasonsToApply.ifEmpty { updatedServiceResult.failureReasons }),
    )
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResult

  companion object {
    /** Returns a ContextUpdater that replaces the current ServiceResult with [result], ignoring the context. */
    fun constant(result: ServiceResult): ContextUpdater = object : ContextUpdater() {
      override fun toServiceResult(context: EvaluationContext): ServiceResult = result
    }

    /** Returns a ContextUpdater that leaves the current ServiceResult unchanged and propagates failure reasons. */
    fun identity(): ContextUpdater = object : ContextUpdater() {
      override val propagatesFailureReasons = true
      override fun toServiceResult(context: EvaluationContext): ServiceResult = context.currentResult.copy(failureReasons = emptyList())
    }
  }
}
