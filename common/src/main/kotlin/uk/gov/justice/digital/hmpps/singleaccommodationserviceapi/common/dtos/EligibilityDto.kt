package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
  // TODO: Remove these defaults once CAS2 is fully rolled out
  val cas2Hdc: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  val cas2PrisonBail: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  val cas2CourtBail: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  val cas3: ServiceResult,
  val caseActions: List<String>,
)

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val suitableApplicationId: UUID? = null,
  val action: String? = null,
  val link: String? = null,
)

enum class ServiceStatus {
  NOT_ELIGIBLE, // NO APPLICATION
  UPCOMING, // NO APPLICATION
  NOT_STARTED,
  NOT_SUBMITTED,
  INFO_REQUESTED,
  REJECTED,
  WITHDRAWN,
  SUBMITTED,
  PLACEMENT_BOOKED,
  CONFIRMED,
  NOT_ARRIVED,
  PLACEMENT_CANCELLED,
  PLACEMENT_REQUEST_NOT_STARTED,
  PLACEMENT_REQUEST_WITHDRAWN,
  PLACEMENT_REQUEST_SUBMITTED,
  PLACEMENT_REQUEST_REJECTED,
  APPLICATION_REJECTED,
  ARRIVED,
  BEDSPACE_OFFERED,
  BOOKING_CONFIRMED,
  BOOKING_CANCELLED,
}
