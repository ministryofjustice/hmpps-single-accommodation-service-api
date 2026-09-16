package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
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

  override val description = set("status from placement / request / application")

  val arrived = "arrived"
  val notArrived = "notArrived"
  val cancelled = "cancelled"
  val placementRequestNotStarted = "placementRequestNotStarted"
  val requestWithdrawn = "requestWithdrawn"
  val requestUnsubmitted = "requestUnsubmitted"
  val requestRejected = "requestRejected"
  val placementRequestSubmitted = "placementRequestSubmitted"
  val infoRequested = "infoRequested"
  val submitted = "submitted"

  override val outcomes = mapOf(
    arrived to ServiceResult(
      serviceStatus = ServiceStatus.ARRIVED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    notArrived to ServiceResult(
      serviceStatus = ServiceStatus.NOT_ARRIVED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    cancelled to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_CANCELLED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    placementRequestNotStarted to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    requestWithdrawn to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    requestUnsubmitted to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    requestRejected to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_REJECTED,
      action = CaseAction(type = CaseActionType.CREATE_PLACEMENT, service = AccommodationService.CAS1),
      link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    placementRequestSubmitted to ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    infoRequested to ServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      action = CaseAction(type = CaseActionType.PROVIDE_INFORMATION, service = AccommodationService.CAS1),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    submitted to ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas1Application?.application?.status
    val requestForPlacementStatus = context.data.cas1Application?.requestForPlacement?.status
    val placementStatus = context.data.cas1Application?.placement?.status

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
    Cas1PlacementStatus.ARRIVED -> outcome(arrived)
    Cas1PlacementStatus.NOT_ARRIVED -> outcome(notArrived)
    Cas1PlacementStatus.CANCELLED -> outcome(cancelled)
    else -> outcome(placementRequestNotStarted)
  }
  private fun toServiceResultBeforePlacement(requestForPlacementStatus: Cas1RequestForPlacementStatus) = when (requestForPlacementStatus) {
    Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> outcome(requestWithdrawn)
    Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED -> outcome(requestUnsubmitted)
    Cas1RequestForPlacementStatus.REQUEST_REJECTED -> outcome(requestRejected)
    else -> outcome(placementRequestSubmitted)
  }

  private fun toServiceResultPriorToPlacementRequest(applicationStatus: Cas1ApplicationStatus?) = when (applicationStatus) {
    Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> outcome(infoRequested)
    else -> outcome(submitted)
  }
}
