package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import java.time.LocalDate

/**
 * Context Updaters are used to update the [EvaluationContext.currentResult] value when rule sets fail
 *
 * There are two pre-defined implementations available:
 * * - [ContextUpdater.constant] - Always set the value of [EvaluationContext.currentResult] to the same given value
 * * - [ContextUpdater.identity] - Leaves the [EvaluationContext.currentResult] as-is
 */
abstract class ContextUpdater {
  open val propagatesFailureReasons: Boolean = false
  open val outcomes: Map<String, ServiceResultSpec> get() = emptyMap()
  open val terminalNode: Boolean get() = false
  open val description: String
    get() = if (terminalNode) {
      val statuses = outcomes.values.map { it.serviceStatus.uiStatusName }.distinct()
      if (statuses.size == 1) {
        "return status ${statuses.first()}"
      } else {
        "return status as one of:\n" + statuses.joinToString(separator = ",\n ")
      }
    } else {
      val statuses = outcomes.values.map { it.serviceStatus.uiStatusName }.distinct()
      if (statuses.size == 1) {
        "Set current status to ${statuses.first()}"
      } else {
        "Set current status to one of:\n" + statuses.joinToString(separator = ",\n ")
      }
    }

  fun update(context: EvaluationContext, failureReasons: List<FailureReason> = emptyList()): EvaluationContext {
    // expose the failing RuleSet failure reasons on the context so updaters can branch on which rules failed
    val contextWithFailureReasons = context.copy(
      currentResult = context.currentResult?.copy(failureReasons = failureReasons),
    )

    val updatedServiceResult = toServiceResult(contextWithFailureReasons)

    val reasonsToApply = if (propagatesFailureReasons) failureReasons else emptyList()

    return context.copy(
      currentResult = updatedServiceResult.copy(failureReasons = reasonsToApply.ifEmpty { updatedServiceResult.failureReasons }),
    )
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResultNew

  protected fun outcome(key: String, actionStartDate: LocalDate? = null): ServiceResultNew = outcomes.getValue(key).toResult(actionStartDate)

  companion object {

    val identity = object : ContextUpdater() {
      override val propagatesFailureReasons = true
      override val description = "identity"
      override fun toServiceResult(context: EvaluationContext): ServiceResultNew = requireNotNull(context.currentResult) {
        "identity ContextUpdater requires currentResult to already be set, but it was null"
      }.copy(failureReasons = emptyList())
    }

    /** Always set the value of [EvaluationContext.currentResult] to the same given value */
    fun constant(result: ServiceResultNew): ContextUpdater = object : ContextUpdater() {
      override val description = "Set ${result.serviceStatus.name}"

      override val outcomes = mapOf(
        "constant" to ServiceResultSpec(
          serviceStatus = result.serviceStatus,
          url = result.url,
          failureReasons = result.failureReasons,
        ),
      )

      override fun toServiceResult(context: EvaluationContext): ServiceResultNew = result
    }

    /** Leaves the [EvaluationContext.currentResult] unchanged and propagates failure reasons. */
    fun identity() = identity
  }
}
