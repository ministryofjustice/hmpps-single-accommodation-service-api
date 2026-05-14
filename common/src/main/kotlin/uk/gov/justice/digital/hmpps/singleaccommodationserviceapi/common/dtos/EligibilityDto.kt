package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: Cas1ServiceResult,
  val cas3: Cas3ServiceResult,
  val dtr: DtrServiceResult,
  val crs: CrsServiceResult,
  val pa: PaServiceResult,
  val caseActions: List<String>,
)

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val action: String? = null,
  val link: String? = null,
  val failureReasons: List<FailureReason> = emptyList(),
)

data class PaServiceResult(
  val serviceResult: ServiceResult,
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
  COMPLETED,
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

enum class FailureReason {
  S_TIER,
  MALE_NOT_HIGH_RISK_TIER,
  NON_MALE_NOT_HIGH_RISK_TIER,
  SEX_DATA_NOT_AVAILABLE,
  NO_CURRENT_ACCOMMODATION_END_DATE,
  INVALID_CURRENT_ACCOMMODATION_TYPE,
  CONFLICTING_CAS1_BOOKING,
  CRS_EXPIRED,
  CRS_NOT_SUBMITTED,
  HAS_NEXT_ACCOMMODATION,
  CURRENT_ADDRESS_IS_PRIVATE,
  DTR_REFERRAL_EXPIRED,
  INVALID_APPLICATION_STATE,
  SUITABLE_CAS1_APPLICATION,
  SUITABLE_CAS3_APPLICATION,
}
