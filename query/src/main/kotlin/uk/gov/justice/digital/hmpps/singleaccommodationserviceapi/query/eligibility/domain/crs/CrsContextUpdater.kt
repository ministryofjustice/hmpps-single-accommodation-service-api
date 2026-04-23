package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsServiceResultTransformer.toCrsServiceResult
import java.time.Clock

@Component
class CrsContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toCrsServiceResult(context.data, clock)
    return context.copy(currentResult = updatedServiceResult)
  }
}
