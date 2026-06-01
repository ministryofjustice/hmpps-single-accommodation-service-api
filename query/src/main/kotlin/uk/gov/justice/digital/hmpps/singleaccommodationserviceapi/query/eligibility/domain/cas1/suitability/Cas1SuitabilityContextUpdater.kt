package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1SuitabilityContextUpdater(
  @Value($$"${service.approved-premises-ui.base-url}") approvedPremisesUiBaseUrl: String,
) : ContextUpdater() {

  val url = approvedPremisesUiBaseUrl

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas1Application?.applicationStatus

    return when (applicationStatus) {
      Cas1ApplicationStatus.STARTED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_SUBMITTED,
        action = EligibilityKeys.CONTINUE_APPROVED_PREMISE_APPLICATION,
        link = EligibilityKeys.CONTINUE_APPLICATION,
        url = url,
      )

      Cas1ApplicationStatus.REJECTED -> ServiceResult(
        serviceStatus = ServiceStatus.APPLICATION_REJECTED,
        action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
        link = EligibilityKeys.START_NEW_APPLICATION,
        url = url,
      )

      else -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
        link = EligibilityKeys.START_APPLICATION,
        url = url,
      )
    }
  }
}
