package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsUpcomingContextUpdater : ContextUpdater() {

  override val description = set("Upcoming and submit CRS referral")

  val upcomingMale = "upcomingMale"
  val upcomingNonMale = "upcomingNonMale"

  override val outcomes = mapOf(
    upcomingMale to ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      action = CaseAction(
        type = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
        service = AccommodationService.CRS,
      ),
    ),
    upcomingNonMale to ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      action = CaseAction(
        type = CaseActionType.SUBMIT_CRS_REFERRAL,
        service = AccommodationService.CRS,
      ),
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val key = if (context.data.sex == SexCode.M) upcomingMale else upcomingNonMale

    return outcome(key).withActionStartDate(
      context.data.currentAccommodation!!.endDate!!.minusWeeks(12),
    )
  }
}
