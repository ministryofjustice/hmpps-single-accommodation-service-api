package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.toServiceStatus
import java.time.Clock

class Cas1ContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(ctx: EvalContext): EvalContext {
    val action = buildAction(ctx.data, clock)

    val updatedServiceResult =
      ServiceResult(
        serviceStatus =
          toServiceStatus(
            ctx.data.cas1Application?.applicationStatus,
            !action.isUpcoming
          ),
        suitableApplicationId = ctx.data.cas1Application?.id,
        action = action
      )

    return ctx.copy(current = updatedServiceResult)
  }
}