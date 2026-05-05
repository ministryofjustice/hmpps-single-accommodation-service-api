package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1SuitabilityContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas1Application?.applicationStatus
    val requestForPlacementStatus = context.data.cas1Application?.requestForPlacementStatus
    val placementStatus = context.data.cas1Application?.placementStatus

    return if (requestForPlacementStatus == null && placementStatus == null) {
      when (applicationStatus) {
        Cas1ApplicationStatus.STARTED -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_SUBMITTED,
          action = EligibilityKeys.CONTINUE_APPROVED_PREMISE_APPLICATION,
          link = EligibilityKeys.CONTINUE_APPLICATION,
        )

        Cas1ApplicationStatus.REJECTED -> ServiceResult(
          serviceStatus = ServiceStatus.APPLICATION_REJECTED,
          action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
          link = EligibilityKeys.START_NEW_APPLICATION,
        )

        Cas1ApplicationStatus.WITHDRAWN,
        Cas1ApplicationStatus.INAPPLICABLE,
        Cas1ApplicationStatus.EXPIRED,
        null,
        -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION,
          link = EligibilityKeys.START_APPLICATION,
        )

        else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
      }
    } else {
      ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
    }
  }
}
