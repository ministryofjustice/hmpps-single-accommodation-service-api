package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val placementStatus: Cas3PlacementStatus?,
)

enum class Cas3ApplicationStatus {
  IN_PROGRESS,
  SUBMITTED,
  REQUESTED_FURTHER_INFORMATION,
  PENDING,
  REJECTED,
  AWAITING_PLACEMENT,
  PLACED,
  INAPPLICABLE,
  WITHDRAWN,
}

enum class Cas3PlacementStatus {
  PROVISIONAL,
  CONFIRMED,
  ARRIVED,
  NOT_ARRIVED,
  DEPARTED,
  CANCELLED,
  CLOSED,
}
