package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums

enum class Cas1ApplicationStatus(val value: String) {
  AWAITING_ASSESSMENT("awaitingAssesment"), // SUBMITTED
  UNALLOCATED_ASSESSMENT("unallocatedAssesment"), // SUBMITTED
  ASSESSMENT_IN_PROGRESS("assesmentInProgress"), // SUBMITTED
  AWAITING_PLACEMENT("awaitingPlacement"), // SUBMITTED
  PLACEMENT_ALLOCATED("placementAllocated"), // CONFIRMED
  REQUEST_FOR_FURTHER_INFORMATION("requestedFurtherInformation"), // SUBMITTED
  PENDING_PLACEMENT_REQUEST("pendingPlacementRequest"), // SUBMITTED
  ;
}
