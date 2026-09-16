package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1UpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and start Approved Premise application")

  val upcoming = "upcoming"

  override val outcomes = mapOf(
    upcoming to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_UPCOMING,
      action = CaseAction(
        type = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
        service = AccommodationService.CAS1,
      ),
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(upcoming).withActionStartDate(
    context.data.currentAccommodation!!.endDate!!.minusYears(1),
  )
}
