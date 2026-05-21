package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildUpcomingAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import java.time.Clock
import java.time.LocalDate

@Component
class CrsUpcomingContextUpdater(val clock: Clock) : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    action = buildUpcomingActionInTwelveWeeks(context),
  )

  private fun buildUpcomingActionInTwelveWeeks(context: EvaluationContext): String {
    val endDate = context.data.currentAccommodation!!.endDate!!
    val dateToStartReferral = endDate.minusWeeks(12)
    val today = LocalDate.now(clock)
    val actionText = if (context.data.sex == SexCode.M) EligibilityKeys.SUBMIT_CRS_ACCOMMODATION_REFERRAL else EligibilityKeys.SUBMIT_CRS_REFERRAL
    return buildUpcomingAction(today, actionText, dateToStartReferral)
  }
}
