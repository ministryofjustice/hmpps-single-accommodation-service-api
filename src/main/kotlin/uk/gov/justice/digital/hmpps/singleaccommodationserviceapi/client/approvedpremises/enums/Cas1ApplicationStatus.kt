package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums

enum class Cas1ApplicationStatus(val value: String) {
  Started("started"), // in draft - forget
  Rejected("rejected"), // forget
  AwaitingAssesment("awaitingAssesment"), // SUBMITTED
  UnallocatedAssesment("unallocatedAssesment"), // SUBMITTED
  AssesmentInProgress("assesmentInProgress"), // SUBMITTED
  AwaitingPlacement("awaitingPlacement"), // SUBMITTED
  PlacementAllocated("placementAllocated"), // CONFIRMED
  Inapplicable("inapplicable"), // forget
  Withdrawn("withdrawn"), // forget
  RequestedFurtherInformation("requestedFurtherInformation"), // SUBMITTED
  PendingPlacementRequest("pendingPlacementRequest"), // SUBMITTED
  Expired("expired"), // forget
}
