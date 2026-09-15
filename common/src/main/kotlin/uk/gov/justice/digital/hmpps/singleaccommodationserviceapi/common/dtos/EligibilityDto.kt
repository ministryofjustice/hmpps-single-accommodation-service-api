package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: Cas1ServiceResult,
  val cas2: Cas2ServiceResult,
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
  val blockingStatusReason: BlockingReason? = null,
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

data class Cas2ServiceResult(
  val serviceResult: ServiceResult,
  val cas2Application: Cas2ApplicationDto?,
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
  UNKNOWN,
  OFFER_DECLINED_OR_WITHDRAWN,
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
  MORE_INFORMATION_NEEDED,
  AWAITING_DECISION,
  ON_WAITING_LIST,
  PLACE_OFFERED,
  OFFER_ACCEPTED,
  CANCELLED,
  AWAITING_ARRIVAL,
}