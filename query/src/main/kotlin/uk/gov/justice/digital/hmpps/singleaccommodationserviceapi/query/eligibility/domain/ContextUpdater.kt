package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

abstract class ContextUpdater {
  fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toServiceResult(context)

    return context.copy(currentResult = updatedServiceResult)
  }

  protected abstract fun toServiceResult(context: EvaluationContext): ServiceResult
}
