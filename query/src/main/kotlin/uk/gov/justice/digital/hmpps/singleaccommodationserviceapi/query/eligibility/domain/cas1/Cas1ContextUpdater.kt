package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ServiceStatusTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import java.time.Clock

@Component
class Cas1ContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val action = Cas1ActionTransformer.buildCas1Action(context.data, clock)

    val updatedServiceResult =
      ServiceResult(
        serviceStatus =
          ServiceStatusTransformer.toServiceStatus(
            context.data.cas1Application?.applicationStatus,
            !action.isUpcoming
          ),
        suitableApplicationId = context.data.cas1Application?.id,
        action = action
      )

    return context.copy(currentResult = updatedServiceResult)
  }
}