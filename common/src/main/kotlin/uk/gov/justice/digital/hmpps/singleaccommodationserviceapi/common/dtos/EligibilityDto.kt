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

enum class ServiceStatus(override val title: String) : TitleEnum {
  UNKNOWN("Unknown"),
  OFFER_DECLINED_OR_WITHDRAWN("Offer declined or withdrawn"),
  NOT_REQUIRED("Not required"),
  NOT_ELIGIBLE("Not eligible"), // NO APPLICATION
  UPCOMING("Upcoming"), // NO APPLICATION
  STARTED("Started"),
  NOT_STARTED("Not started"),
  NOT_SUBMITTED("Not submitted"),
  INFO_REQUESTED("Info requested"),
  COMPLETED("Completed"),
  REJECTED("Rejected"),
  WITHDRAWN("Withdrawn"),
  SUBMITTED("Submitted"),
  PLACEMENT_BOOKED("Placement booked"),
  CONFIRMED("Confirmed"),
  NOT_ARRIVED("Not arrived"),
  PLACEMENT_CANCELLED("Placement cancelled"),
  PLACEMENT_REQUEST_NOT_STARTED("Placement request not started"),
  PLACEMENT_REQUEST_WITHDRAWN("Placement request withdrawn"),
  PLACEMENT_REQUEST_SUBMITTED("Placement request submitted"),
  PLACEMENT_REQUEST_REJECTED("Placement request rejected"),
  APPLICATION_REJECTED("Application rejected"),
  ARRIVED("Arrived"),
  BEDSPACE_OFFERED("Bedspace offered"),
  BOOKING_CONFIRMED("Booking confirmed"),
  BOOKING_CANCELLED("Booking cancelled"),
  ACCEPTED("Accepted"),
  NOT_ACCEPTED("Not accepted"),
  CANNOT_START_YET("Cannot start yet"),
  MORE_INFORMATION_NEEDED("More information needed"),
  AWAITING_DECISION("Awaiting decision"),
  ON_WAITING_LIST("On waiting list"),
  PLACE_OFFERED("Place offert"),
  OFFER_ACCEPTED("Offer accepted"),
  CANCELLED("Cancelled"),
  AWAITING_ARRIVAL("Awaiting arrival"),
}

enum class LinkType {
  CAS1_START_APPLICATION,
  CAS1_VIEW_APPLICATION,
  CAS2_START_APPLICATION,
  CAS2_VIEW_APPLICATION,
  CAS3_START_REFERRAL,
  CAS3_VIEW_REFERRAL,
}

enum class FailureReason {
  S_TIER,
  MALE_NOT_HIGH_RISK_TIER,
  NON_MALE_NOT_HIGH_RISK_TIER,
  SEX_DATA_NOT_AVAILABLE,
  INVALID_CURRENT_ACCOMMODATION_TYPE,
  CRS_NOT_SUBMITTED,
  CRS_NOT_SUBMITTED_MALE,
  CRS_NOT_SUBMITTED_NON_MALE,
  HAS_NEXT_ACCOMMODATION,
  DTR_REFERRAL_EXPIRED,
  SUITABLE_CAS1_APPLICATION,
  SUITABLE_CAS3_APPLICATION,
  IS_SETTLED,
}

enum class BlockingReason {
  // CAS3 PREREQUISITES
  SUBMIT_DTR_BEFORE_CAS3,
  SUBMIT_CRS_BEFORE_CAS3,
  SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3,
  SUBMIT_DTR_AND_CRS_BEFORE_CAS3,
  SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3,
}
