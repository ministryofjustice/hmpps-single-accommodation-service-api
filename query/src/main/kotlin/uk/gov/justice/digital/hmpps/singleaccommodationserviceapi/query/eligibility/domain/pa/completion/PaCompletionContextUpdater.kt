package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class PaCompletionContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    action = EligibilityKeys.ADD_AND_CONFIRM_PROPOSED_ADDRESS,
  )
}
