package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class DtrCompletionContextUpdater : ContextUpdater() {

  override val description = set("status from DTR")

  val notAccepted = "notAccepted"
  val submitted = "submitted"

  override val outcomes = mapOf(
    notAccepted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.DTR_NOT_ACCEPTED,
    ),
    submitted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.DTR_SUBMITTED,
      action = CaseAction(type = CaseActionType.ADD_DTR_OUTCOME, service = AccommodationService.DTR),
      link = EligibilityKeys.ADD_OUTCOME,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = when (context.data.dutyToRefer?.status) {
    DtrStatus.NOT_ACCEPTED -> outcome(notAccepted)
    else -> outcome(submitted)
  }
}
