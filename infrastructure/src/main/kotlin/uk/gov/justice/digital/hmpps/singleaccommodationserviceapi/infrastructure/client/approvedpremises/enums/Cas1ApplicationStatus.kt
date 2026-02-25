package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums

enum class Cas1ApplicationStatus(val value: String) {
  AWAITING_ASSESSMENT("awaitingAssesment"),
  UNALLOCATED_ASSESSMENT("unallocatedAssesment"),
  ASSESSMENT_IN_PROGRESS("assesmentInProgress"),
  AWAITING_PLACEMENT("awaitingPlacement"),
  PLACEMENT_ALLOCATED("placementAllocated"),
  REQUEST_FOR_FURTHER_INFORMATION("requestedFurtherInformation"),
  PENDING_PLACEMENT_REQUEST("pendingPlacementRequest"),
  STARTED("started"),
  REJECTED("rejected"),
  INAPPLICABLE("inapplicable"),
  WITHDRAWN("withdrawn"),
  EXPIRED("expired"),
}
