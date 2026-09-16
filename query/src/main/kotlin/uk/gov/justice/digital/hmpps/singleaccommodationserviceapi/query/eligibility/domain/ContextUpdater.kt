package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import java.time.LocalDate

abstract class ContextUpdater {
  open val propagatesFailureReasons: Boolean = false
  open val description: String get() = this::class.simpleName ?: "Update result"
  open val outcomes: Map<String, ServiceResultNew> get() = emptyMap()

  fun update(context: EvaluationContext, failureReasons: List<FailureReason> = emptyList()): EvaluationContext {
    // expose the failing RuleSet failure reasons on the context so updaters can branch on which rules failed
    val contextWithFailureReasons = context.copy(
      currentResult = context.currentResult.copy(failureReasons = failureReasons),
    )
    val updatedServiceResult = toServiceResult(contextWithFailureReasons)
    val reasonsToApply = if (propagatesFailureReasons) failureReasons else emptyList()

    return context.copy(
      currentResult = updatedServiceResult.copy(failureReasons = reasonsToApply.ifEmpty { updatedServiceResult.failureReasons }),
    )
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResultNew

  protected fun outcome(key: String): ServiceResultNew = outcomes.getValue(key)

  protected fun ServiceResultNew.withActionStartDate(startDate: LocalDate): ServiceResultNew = copy(action = action?.copy(startDate = startDate))

  protected fun set(text: String): String = "Set $text"

  companion object {
    /** Returns a ContextUpdater that replaces the current ServiceResult with [result], ignoring the context. */
    fun constant(result: ServiceResultNew): ContextUpdater = object : ContextUpdater() {
      override val description = set(result.serviceStatus.name)
      override val outcomes = mapOf("constant" to result)
      override fun toServiceResult(context: EvaluationContext): ServiceResultNew = outcome("constant")
    }

    /** Returns a ContextUpdater that leaves the current ServiceResult unchanged and propagates failure reasons. */
    fun identity(): ContextUpdater = object : ContextUpdater() {
      override val propagatesFailureReasons = true
      override val description = "identity"
      override fun toServiceResult(context: EvaluationContext): ServiceResultNew = context.currentResult.copy(failureReasons = emptyList())
    }
  }
}
