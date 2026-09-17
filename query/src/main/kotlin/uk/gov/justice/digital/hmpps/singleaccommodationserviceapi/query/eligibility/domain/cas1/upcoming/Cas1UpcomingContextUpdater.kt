package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1UpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and start Approved Premise application")

  val upcoming = "upcoming"

  override val outcomes = mapOf(
    upcoming to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_UPCOMING,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(
    key = upcoming,
    actionStartDate = context.data.currentAccommodation!!.endDate!!.minusYears(1),
  )
}
