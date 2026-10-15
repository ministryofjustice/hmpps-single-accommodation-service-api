package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class DtrUpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and submit DTR referral")

  val upcoming = "upcoming"

  override val outcomes = mapOf(
    upcoming to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.DTR_UPCOMING,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(
    key = upcoming,
    actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(8),
  )
}
