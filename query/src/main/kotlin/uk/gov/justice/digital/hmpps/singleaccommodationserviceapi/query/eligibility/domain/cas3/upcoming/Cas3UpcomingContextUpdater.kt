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
class Cas3UpcomingContextUpdater(val clock: Clock) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val today = LocalDate.now(clock)

    val updatedServiceResult = ServiceResult(
      serviceStatus = ServiceStatus.UPCOMING,
      action = buildUpcomingAction(context.data.currentAccommodation!!.endDate!!, today, EligibilityKeys.START_REFERRAL),
    )
    return context.copy(currentResult = updatedServiceResult)
  }
}
