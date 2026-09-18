package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class DtrUpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and submit DTR referral")

  val upcoming = "upcoming"

  override val outcomes = mapOf(
    upcoming to ServiceResultNew(
      serviceStatus = ServiceStatusNew.DTR_UPCOMING,
      action = CaseAction(
        type = CaseActionType.SUBMIT_DTR_REFERRAL,
        service = AccommodationService.DTR,
      ),
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(upcoming).withActionStartDate(
    context.data.currentAccommodation!!.endDate!!.minusWeeks(8),
  )
}
