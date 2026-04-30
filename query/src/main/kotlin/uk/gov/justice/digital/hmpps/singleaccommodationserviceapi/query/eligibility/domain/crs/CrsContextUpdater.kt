package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsContextUpdater : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = EligibilityKeys.COMPLETE_CRS_REFERRAL,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    )
    return context.copy(currentResult = updatedServiceResult)
  }
}
