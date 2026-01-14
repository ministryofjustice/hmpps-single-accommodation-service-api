package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.toServiceStatus

class Cas1ContextUpdater : ContextUpdater {
  override fun update(ctx: EvalContext, result: RuleSetResult): EvalContext {
    val hasImminentActions = result.actions.any { !it.isUpcoming }

    val updatedServiceResult =
      ServiceResult(
        serviceStatus =
          toServiceStatus(
            ctx.data.cas1Application?.applicationStatus,
            hasImminentActions
          ),
        suitableApplicationId = ctx.data.cas1Application?.id,
        actions = result.actions
      )

    return ctx.copy(current = updatedServiceResult)
  }
}