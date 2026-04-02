package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val assessmentStatus: Cas3AssessmentStatus?,
  val bookingStatus: Cas3BookingStatus?,
)

enum class Cas3ApplicationStatus {
  IN_PROGRESS,
  SUBMITTED,
  REQUESTED_FURTHER_INFORMATION,
  REJECTED,
}

enum class Cas3AssessmentStatus {
  UNALLOCATED,
  IN_REVIEW,
  READY_TO_PLACE,
  CLOSED,
  REJECTED,
}

enum class Cas3BookingStatus {
  PROVISIONAL,
  CONFIRMED,
  ARRIVED,
  NOT_MINUS_ARRIVED,
  DEPARTED,
  CANCELLED,
  CLOSED,
}
