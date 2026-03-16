package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.util.UUID

data class Cas1Application(
  val crn: String,
  val id: UUID,
  val applicationStatus: Cas1ApplicationStatus,
  val requestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val placementStatus: Cas1PlacementStatus?,
)

enum class Cas1RequestForPlacementStatus {
  REQUEST_UNSUBMITTED,
  REQUEST_REJECTED,
  REQUEST_SUBMITTED,
  AWAITING_MATCH,
  REQUEST_WITHDRAWN,
  PLACEMENT_BOOKED,
}

enum class Cas1PlacementStatus {
  ARRIVED,
  UPCOMING,
  DEPARTED,
  NOT_ARRIVED,
  CANCELLED,
}

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
