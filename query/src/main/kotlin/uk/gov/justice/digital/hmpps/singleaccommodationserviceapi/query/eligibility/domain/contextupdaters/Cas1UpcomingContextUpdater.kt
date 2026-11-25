package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CAS1_UPCOMING_WEEKS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas1UpcomingContextUpdater : ContextUpdater() {

  val upcoming = "upcoming"

  override val terminalNode = true

  override val outcomes = mapOf(
    upcoming to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS1_UPCOMING,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(
    key = upcoming,
    actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(
      CAS1_UPCOMING_WEEKS,
    ),
  )
}
