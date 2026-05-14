package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming

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
class Cas3UpcomingContextUpdater(val clock: Clock) : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    action = buildUpcomingActionInFourWeeks(context),
  )

  private fun buildUpcomingActionInFourWeeks(context: EvaluationContext): String {
    val endDate = context.data.currentAccommodation!!.endDate!!
    val dateToStartReferral = endDate.minusWeeks(4)
    val today = LocalDate.now(clock)
    return buildUpcomingAction(today, EligibilityKeys.START_CAS3_REFERRAL, dateToStartReferral)
  }
}
