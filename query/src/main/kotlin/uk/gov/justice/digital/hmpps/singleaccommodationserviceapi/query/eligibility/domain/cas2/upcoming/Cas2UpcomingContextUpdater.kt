package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2UpcomingContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    action = CaseAction(
      type = CaseActionType.START_CAS2_APPLICATION,
      startDate = context.data.currentAccommodation!!.endDate!!.minusYears(1),
    ),
  )
}
