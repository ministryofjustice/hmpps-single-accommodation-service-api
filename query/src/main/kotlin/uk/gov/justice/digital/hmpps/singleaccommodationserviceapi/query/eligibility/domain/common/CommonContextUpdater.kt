package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CommonContextUpdater : ContextUpdater() {
  override val propagatesFailureReasons = true

  override fun toServiceResult(context: EvaluationContext) = context.currentResult.copy(failureReasons = emptyList())
}
