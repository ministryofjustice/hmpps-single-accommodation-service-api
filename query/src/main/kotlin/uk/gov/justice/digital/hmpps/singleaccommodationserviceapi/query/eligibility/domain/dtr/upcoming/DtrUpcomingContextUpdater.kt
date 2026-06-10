package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildUpcomingAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import java.time.Clock
import java.time.LocalDate

@Component
class DtrUpcomingContextUpdater(val clock: Clock) : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    action = buildUpcomingActionInEightWeeks(context),
  )

  private fun buildUpcomingActionInEightWeeks(context: EvaluationContext): String {
    val endDate = context.data.currentAccommodation!!.endDate!!
    val dateToStartReferral = endDate.minusWeeks(8)
    val today = LocalDate.now(clock)
    return buildUpcomingAction(today, EligibilityKeys.SUBMIT_DTR_REFERRAL, dateToStartReferral)
  }
}
