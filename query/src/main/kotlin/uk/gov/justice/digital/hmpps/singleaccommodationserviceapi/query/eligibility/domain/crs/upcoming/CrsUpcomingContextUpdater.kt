package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsUpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and submit CRS referral")

  val upcomingMale = "upcomingMale"
  val upcomingNonMale = "upcomingNonMale"

  override val outcomes = mapOf(
    upcomingMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_UPCOMING_ACCOMMODATION_REFERRAL,
    ),
    upcomingNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_UPCOMING_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val key = if (context.data.sex == SexCode.M) upcomingMale else upcomingNonMale

    return outcome(
      key = key,
      actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(12),
    )
  }
}
