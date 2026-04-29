package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: Cas1ServiceResult,
  val cas3: Cas3ServiceResult,
  val dtr: DtrServiceResult,
  val crs: CrsServiceResult,
  val caseActions: List<String>,
)

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val action: String? = null,
  val link: String? = null,
)

data class DtrServiceResult(
  val serviceResult: ServiceResult,
  val caseId: UUID?,
  val submission: DtrSubmissionDto?,
)

data class Cas1ServiceResult(
  val serviceResult: ServiceResult,
  val cas1Application: Cas1ApplicationDto?,
)

data class Cas3ServiceResult(
  val serviceResult: ServiceResult,
  val cas3Application: Cas3ApplicationDto?,
)

data class CrsServiceResult(
  val serviceResult: ServiceResult,
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto?,
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
  ACCEPTED,
  NOT_ACCEPTED,
}
