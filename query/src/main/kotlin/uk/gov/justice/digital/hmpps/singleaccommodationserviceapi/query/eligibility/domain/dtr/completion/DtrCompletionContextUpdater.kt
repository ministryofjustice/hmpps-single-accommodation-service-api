package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class DtrCompletionContextUpdater : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = when (context.data.dtrStatus) {
      DtrStatus.NOT_ACCEPTED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_ACCEPTED,
      )

      else -> ServiceResult(
        serviceStatus = ServiceStatus.SUBMITTED,
        action = EligibilityKeys.ADD_DTR_OUTCOME,
        link = EligibilityKeys.ADD_OUTCOME,
      )
    }
    return context.copy(currentResult = updatedServiceResult)
  }
}
