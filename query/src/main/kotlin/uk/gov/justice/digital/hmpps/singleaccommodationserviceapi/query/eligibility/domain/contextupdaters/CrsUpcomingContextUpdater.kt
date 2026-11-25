package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class CrsUpcomingContextUpdater : ContextUpdater() {

  override val terminalNode = true

  val upcomingMale = "upcomingMale"
  val upcomingNonMale = "upcomingNonMale"

  override val outcomes = mapOf(
    upcomingMale to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CRS_UPCOMING_ACCOMMODATION_REFERRAL,
    ),
    upcomingNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CRS_UPCOMING_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultV2 {
    val key = if (context.data.sex == SexCode.M) upcomingMale else upcomingNonMale

    return outcome(
      key = key,
      actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(12),
    )
  }
}
