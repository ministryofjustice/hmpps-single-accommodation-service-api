package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
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
    arrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_ARRIVED,
    ),
    notArrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_NOT_ARRIVED,
    ),
    cancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_CANCELLED,
    ),
    placementRequestNotStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED,
    ),
    requestWithdrawn to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_WITHDRAWN,
    ),
    requestUnsubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED,
    ),
    requestRejected to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_REJECTED,
    ),
    placementRequestSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_SUBMITTED,
    ),
    infoRequested to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_INFO_REQUESTED,
    ),
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_SUBMITTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
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
