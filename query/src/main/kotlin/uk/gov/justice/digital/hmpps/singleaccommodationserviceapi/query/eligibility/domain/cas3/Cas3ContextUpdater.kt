package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ServiceResultTransformer.toCas3ServiceResult
import java.time.Clock

@Component
class Cas3ContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toCas3ServiceResult(context.data)
    return context.copy(currentResult = updatedServiceResult)
  }
}
