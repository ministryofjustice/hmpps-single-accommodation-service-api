package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsCompletionContextUpdater(
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : ContextUpdater() {

  val url = crsUiBaseUrl

  override val description = set("Not started and submit CRS referral")

  val notStartedMale = "notStartedMale"
  val notStartedNonMale = "notStartedNonMale"

  override val outcomes = mapOf(
    notStartedMale to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(
        type = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
        service = AccommodationService.CRS,
      ),
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      url = url,
    ),
    notStartedNonMale to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(
        type = CaseActionType.SUBMIT_CRS_REFERRAL,
        service = AccommodationService.CRS,
      ),
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      url = url,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = if (context.data.sex == SexCode.M) {
    outcome(notStartedMale)
  } else {
    outcome(notStartedNonMale)
  }
}
