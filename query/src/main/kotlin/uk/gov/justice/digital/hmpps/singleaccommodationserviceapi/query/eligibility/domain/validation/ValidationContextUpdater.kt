package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class ValidationContextUpdater : ContextUpdater {
  override fun update(context: EvaluationContext) = context
}
