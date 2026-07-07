package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: Cas1ServiceResult,
  val cas3: Cas3ServiceResult,
  val dtr: DtrServiceResult,
  val crs: CrsServiceResult,
  val pa: PaServiceResult,
  val caseActions: List<CaseAction>,
)

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val action: CaseAction? = null,
  val link: String? = null,
  val url: String? = null,
  val linkType: LinkType? = null,
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
  NOT_REQUIRED,
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
  CANNOT_START_YET,
}

enum class LinkType {
  CAS1_START_APPLICATION,
  CAS1_VIEW_APPLICATION,
  CAS3_START_REFERRAL,
  CAS3_VIEW_REFERRAL,
  CRS_START_REFERRAL,
  CRS_VIEW_REFERRAL,
}

enum class FailureReason {
  S_TIER,
  MALE_NOT_HIGH_RISK_TIER,
  NON_MALE_NOT_HIGH_RISK_TIER,
  SEX_DATA_NOT_AVAILABLE,
  INVALID_CURRENT_ACCOMMODATION_TYPE,
  CRS_EXPIRED,
  CRS_NOT_SUBMITTED,
  HAS_NEXT_ACCOMMODATION,
  DTR_REFERRAL_EXPIRED,
  INVALID_APPLICATION_STATE,
  SUITABLE_CAS1_APPLICATION,
  SUITABLE_CAS3_APPLICATION,
}
