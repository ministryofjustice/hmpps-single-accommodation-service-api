package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

abstract class ContextUpdater {
  fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toServiceResult(context)

    return context.copy(currentResult = updatedServiceResult)
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
