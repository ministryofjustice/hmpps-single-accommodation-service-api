package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1CompletionContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas1Application?.applicationStatus
    val requestForPlacementStatus = context.data.cas1Application?.requestForPlacementStatus
    val placementStatus = context.data.cas1Application?.placementStatus

    return when {
      placementStatus != null ->
        toServiceResultAfterPlacement(placementStatus)

      requestForPlacementStatus != null ->
        toServiceResultBeforePlacement(requestForPlacementStatus)

      else ->
        toServiceResultPriorToPlacementRequest(applicationStatus)
    }
  }

  private fun toServiceResultAfterPlacement(placementStatus: Cas1PlacementStatus) = when (placementStatus) {
    Cas1PlacementStatus.ARRIVED -> ServiceResult(
      serviceStatus = ServiceStatus.ARRIVED,
      action = null,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    Cas1PlacementStatus.NOT_ARRIVED -> ServiceResult(
      serviceStatus = ServiceStatus.NOT_ARRIVED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    Cas1PlacementStatus.CANCELLED -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_CANCELLED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    else -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )
  }

  private fun toServiceResultBeforePlacement(requestForPlacementStatus: Cas1RequestForPlacementStatus) = when (requestForPlacementStatus) {
    Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    Cas1RequestForPlacementStatus.REQUEST_REJECTED -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_REJECTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    else -> ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )
  }

  private fun toServiceResultPriorToPlacementRequest(applicationStatus: Cas1ApplicationStatus?) = when (applicationStatus) {
    Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> ServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      action = CaseAction(type = CaseActionType.PROVIDE_INFORMATION),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )

    else -> ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    )
  }
}
