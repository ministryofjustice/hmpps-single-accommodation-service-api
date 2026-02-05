package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import java.time.Clock

@Component
class Cas3ContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val action = buildCas3Action(context.data, clock)

    val updatedServiceResult = ServiceResult(
      serviceStatus = Cas3ServiceStatusTransformer.toServiceStatus(context.data.cas3Application?.applicationStatus, action.isUpcoming),
      suitableApplicationId = context.data.cas3Application?.id,
      action = action,
    )

    return context.copy(currentResult = updatedServiceResult)
  }
}
