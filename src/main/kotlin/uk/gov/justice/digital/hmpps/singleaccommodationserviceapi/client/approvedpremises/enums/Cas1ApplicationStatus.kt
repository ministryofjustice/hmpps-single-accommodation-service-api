package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus

enum class Cas1ApplicationStatus(val value: String) {
  AWAITING_ASSESSMENT("awaitingAssesment"), // SUBMITTED
  UNALLOCATED_ASSESSMENT("unallocatedAssesment"), // SUBMITTED
  ASSESSMENT_IN_PROGRESS("assesmentInProgress"), // SUBMITTED
  AWAITING_PLACEMENT("awaitingPlacement"), // SUBMITTED
  PLACEMENT_ALLOCATED("placementAllocated"), // CONFIRMED
  REQUEST_FOR_FURTHER_INFORMATION("requestedFurtherInformation"), // SUBMITTED
  PENDING_PLACEMENT_REQUEST("pendingPlacementRequest"), // SUBMITTED
  ;

  fun toServiceStatus() = when (this) {
    PLACEMENT_ALLOCATED -> null
    AWAITING_ASSESSMENT -> ServiceStatus.AWAITING_ASSESSMENT
    UNALLOCATED_ASSESSMENT -> ServiceStatus.UNALLOCATED_ASSESSMENT
    ASSESSMENT_IN_PROGRESS -> ServiceStatus.ASSESSMENT_IN_PROGRESS
    AWAITING_PLACEMENT -> ServiceStatus.AWAITING_PLACEMENT
    REQUEST_FOR_FURTHER_INFORMATION -> ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION
    PENDING_PLACEMENT_REQUEST -> ServiceStatus.PENDING_PLACEMENT_REQUEST
  }
}
