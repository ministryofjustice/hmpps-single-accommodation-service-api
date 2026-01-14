package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater

class Cas2ContextUpdater(
  private val applicationId: java.util.UUID?
) : ContextUpdater {
  override fun update(ctx: EvalContext, result: RuleSetResult): EvalContext {
    val updatedServiceResult = ServiceResult(
      serviceStatus = ServiceStatus.CONFIRMED,
      suitableApplicationId = applicationId,
      actions = result.actions
    )

    return ctx.copy(current = updatedServiceResult)
  }
}