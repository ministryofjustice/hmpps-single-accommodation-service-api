package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.util.UUID

data class Cas1Application(
  val id: UUID,
  val applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.Started,
  val placementStatus: Cas1PlacementStatus = Cas1PlacementStatus.NOT_ALLOCATED,
) {
  fun transformToServiceStatus() = when (applicationStatus) {
    Cas1ApplicationStatus.PlacementAllocated -> transformPlacementStatusToServiceStatus()
    Cas1ApplicationStatus.AwaitingAssesment -> ServiceStatus.AWAITING_ASSESSMENT
    Cas1ApplicationStatus.UnallocatedAssesment -> ServiceStatus.UNALLOCATED_ASSESSMENT
    Cas1ApplicationStatus.AssesmentInProgress -> ServiceStatus.ASSESSMENT_IN_PROGRESS
    Cas1ApplicationStatus.AwaitingPlacement -> ServiceStatus.AWAITING_PLACEMENT
    Cas1ApplicationStatus.RequestedFurtherInformation -> ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION
    Cas1ApplicationStatus.PendingPlacementRequest -> ServiceStatus.PENDING_PLACEMENT_REQUEST
    else -> error("Unexpected application status: $applicationStatus")
  }

  private fun transformPlacementStatusToServiceStatus() = when (placementStatus) {
    Cas1PlacementStatus.UPCOMING -> ServiceStatus.UPCOMING_PLACEMENT
    Cas1PlacementStatus.ARRIVED -> ServiceStatus.ARRIVED
    Cas1PlacementStatus.DEPARTED -> ServiceStatus.DEPARTED
    Cas1PlacementStatus.NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    Cas1PlacementStatus.CANCELLED -> ServiceStatus.CANCELLED
    else -> error("Unexpected placement status: $placementStatus")
  }

  fun buildActions(): List<String> {
    val expiredPlacementStatus = listOf(
      Cas1PlacementStatus.CANCELLED,
      Cas1PlacementStatus.DEPARTED,
      Cas1PlacementStatus.NOT_ALLOCATED,
      Cas1PlacementStatus.NOT_ARRIVED,
    )
    val actions = mutableListOf<String>()
    val isExpiredPlacement =
      applicationStatus == Cas1ApplicationStatus.PlacementAllocated &&
        expiredPlacementStatus.contains(placementStatus)
    if (applicationStatus != Cas1ApplicationStatus.PlacementAllocated || isExpiredPlacement) {
      actions.add("Create a placement request.")
    }
    return actions
  }
}
