package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ServiceResultTransformer.toCas1ServiceResult
import java.time.Clock

@Component
class Cas1ContextUpdater(val clock: Clock) : ContextUpdater {

  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toCas1ServiceResult(context.data, clock)

    return context.copy(currentResult = updatedServiceResult)
  }
}
