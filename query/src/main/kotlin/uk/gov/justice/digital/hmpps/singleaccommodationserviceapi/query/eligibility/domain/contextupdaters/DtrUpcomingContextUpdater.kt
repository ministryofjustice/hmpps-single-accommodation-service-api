package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CurrentAccommodationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class DtrUpcomingContextUpdater : ContextUpdater<CurrentAccommodationData>() {
  override val inputFields = setOf(DomainDataField.CURRENT_ACCOMMODATION_END_DATE)
  override val terminalNode = true
  val upcoming = "upcoming"

  override val outcomes = mapOf(
    upcoming to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.DTR_UPCOMING,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<CurrentAccommodationData>) = outcome(
    key = upcoming,
    actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(8),
  )
}
