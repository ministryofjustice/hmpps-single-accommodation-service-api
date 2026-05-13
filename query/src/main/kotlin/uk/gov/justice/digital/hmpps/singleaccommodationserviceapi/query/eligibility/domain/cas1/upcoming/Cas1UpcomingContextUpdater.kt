package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming

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
class Cas1UpcomingContextUpdater(val clock: Clock) : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.UPCOMING,
    action = buildUpcomingActionInOneYear(context),
  )

  private fun buildUpcomingActionInOneYear(context: EvaluationContext): String {
    val endDate = context.data.currentAccommodation!!.endDate!!
    val dateToStartReferral = endDate.minusYears(1)
    val today = LocalDate.now(clock)
    return buildUpcomingAction(today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION, dateToStartReferral)
  }
}
