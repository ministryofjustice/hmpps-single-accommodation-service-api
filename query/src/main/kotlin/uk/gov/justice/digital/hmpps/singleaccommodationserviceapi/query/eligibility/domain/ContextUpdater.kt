package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

abstract class ContextUpdater {
  fun update(context: EvaluationContext, failureReasons: List<FailureReason> = emptyList()): EvaluationContext {
    val updatedServiceResult = toServiceResult(context)

    return context.copy(
      currentResult = updatedServiceResult.copy(failureReasons = failureReasons.ifEmpty { updatedServiceResult.failureReasons }),
    )
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResult
}
