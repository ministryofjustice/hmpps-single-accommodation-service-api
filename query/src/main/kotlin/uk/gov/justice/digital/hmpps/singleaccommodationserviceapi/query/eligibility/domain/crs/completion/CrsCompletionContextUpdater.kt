package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.mapSexToCaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsCompletionContextUpdater(
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : ContextUpdater() {

  val url = crsUiBaseUrl

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    action = CaseAction(
      type = mapSexToCaseActionType(context.data.sex),
    ),
    link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    url = url,
  )
}
