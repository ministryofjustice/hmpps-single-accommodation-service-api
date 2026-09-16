package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDtoNew(
  val crn: String,
  val cas1: Cas1ServiceResultNew,
  val cas2: Cas2ServiceResultNew,
  val cas3: Cas3ServiceResultNew,
  val dtr: DtrServiceResultNew,
  val crs: CrsServiceResultNew,
  val pa: PaServiceResultNew,
  val caseActions: List<CaseAction>,
)

data class ServiceResultNew(
  val serviceStatus: ServiceStatusNew,
  val action: CaseAction? = null,
  val link: String? = null,
  val url: String? = null,
  val linkType: LinkType? = null,
  val failureReasons: List<FailureReason> = emptyList(),
  val blockingStatusReason: BlockingReason? = null,
)

data class PaServiceResultNew(
  val serviceResult: ServiceResultNew,
)

data class DtrServiceResultNew(
  val serviceResult: ServiceResultNew,
  val caseId: UUID?,
  val submission: DtrSubmissionDto?,
)

data class Cas1ServiceResultNew(
  val serviceResult: ServiceResultNew,
  val cas1Application: Cas1ApplicationDto?,
)

data class Cas2ServiceResultNew(
  val serviceResult: ServiceResultNew,
  val cas2Application: Cas2ApplicationDto?,
)

data class Cas3ServiceResultNew(
  val serviceResult: ServiceResultNew,
  val cas3Application: Cas3ApplicationDto?,
)

data class CrsServiceResultNew(
  val serviceResult: ServiceResultNew,
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto?,
)

enum class ServiceStatusNew(
  val service: AccommodationService,
) {

  // CAS1 Service Statuses
  CAS1_PLACEMENT_BOOKED(
    service = AccommodationService.CAS1,
  ),
  CAS1_UPCOMING(
    service = AccommodationService.CAS1,
  ),
  CAS1_NOT_STARTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_NOT_SUBMITTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_INFO_REQUESTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_SUBMITTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_NOT_ARRIVED(
    service = AccommodationService.CAS1,
  ),
  CAS1_PLACEMENT_CANCELLED(
    service = AccommodationService.CAS1,
  ),
  CAS1_PLACEMENT_REQUEST_NOT_STARTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_PLACEMENT_REQUEST_WITHDRAWN(
    service = AccommodationService.CAS1,
  ),
  CAS1_PLACEMENT_REQUEST_SUBMITTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_PLACEMENT_REQUEST_REJECTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_APPLICATION_REJECTED(
    service = AccommodationService.CAS1,
  ),
  CAS1_ARRIVED(
    service = AccommodationService.CAS1,
  ),
  CAS1_NOT_ELIGIBLE(
    service = AccommodationService.CAS1,
  ),

  // CAS2 Service Statuses
  CAS2_UNKNOWN(
    service = AccommodationService.CAS2,
  ),
  CAS2_OFFER_DECLINED_OR_WITHDRAWN(
    service = AccommodationService.CAS2,
  ),
  CAS2_SUBMITTED(
    service = AccommodationService.CAS2,
  ),
  CAS2_NOT_STARTED(
    service = AccommodationService.CAS2,
  ),
  CAS2_NOT_SUBMITTED(
    service = AccommodationService.CAS2,
  ),
  CAS2_UPCOMING(
    service = AccommodationService.CAS2,
  ),
  CAS2_MORE_INFORMATION_NEEDED(
    service = AccommodationService.CAS2,
  ),
  CAS2_AWAITING_DECISION(
    service = AccommodationService.CAS2,
  ),
  CAS2_ON_WAITING_LIST(
    service = AccommodationService.CAS2,
  ),
  CAS2_PLACE_OFFERED(
    service = AccommodationService.CAS2,
  ),
  CAS2_OFFER_ACCEPTED(
    service = AccommodationService.CAS2,
  ),
  CAS2_CANCELLED(
    service = AccommodationService.CAS2,
  ),
  CAS2_AWAITING_ARRIVAL(
    service = AccommodationService.CAS2,
  ),
  CAS2_WITHDRAWN(
    service = AccommodationService.CAS2,
  ),
  CAS2_NOT_ELIGIBLE(
    service = AccommodationService.CAS2,
  ),

  // CAS3 Service Statuses
  CAS3_NOT_ARRIVED(
    service = AccommodationService.CAS3,
  ),
  CAS3_SUBMITTED(
    service = AccommodationService.CAS3,
  ),
  CAS3_NOT_STARTED(
    service = AccommodationService.CAS3,
  ),
  CAS3_NOT_SUBMITTED(
    service = AccommodationService.CAS3,
  ),
  CAS3_REJECTED(
    service = AccommodationService.CAS3,
  ),
  CAS3_BEDSPACE_OFFERED(
    service = AccommodationService.CAS3,
  ),
  CAS3_BOOKING_CONFIRMED(
    service = AccommodationService.CAS3,
  ),
  CAS3_BOOKING_CANCELLED(
    service = AccommodationService.CAS3,
  ),
  CAS3_CANNOT_START_YET(
    service = AccommodationService.CAS3,
  ),
  CAS3_NOT_ELIGIBLE(
    service = AccommodationService.CAS3,
  ),

  // CRS Service Statuses
  CRS_NOT_STARTED(
    service = AccommodationService.CRS,
  ),
  CRS_UPCOMING(
    service = AccommodationService.CRS,
  ),
  CRS_SUBMITTED(
    service = AccommodationService.CRS,
  ),
  CRS_NOT_REQUIRED(
    service = AccommodationService.CRS,
  ),
  CRS_NOT_ELIGIBLE(
    service = AccommodationService.CRS,
  ),

  // DTR Service Statuses
  DTR_SUBMITTED(
    service = AccommodationService.DTR,
  ),
  DTR_UPCOMING(
    service = AccommodationService.DTR,
  ),
  DTR_NOT_STARTED(
    service = AccommodationService.DTR,
  ),
  DTR_ACCEPTED(
    service = AccommodationService.DTR,
  ),
  DTR_NOT_ACCEPTED(
    service = AccommodationService.DTR,
  ),
  DTR_NOT_REQUIRED(
    service = AccommodationService.DTR,
  ),
  DTR_NOT_ELIGIBLE(
    service = AccommodationService.DTR,
  ),

  // PA Service Statuses
  PA_NOT_STARTED(
    service = AccommodationService.PA,
  ),
  PA_NOT_ELIGIBLE(
    service = AccommodationService.PA,
  ),
  PA_COMPLETED(
    service = AccommodationService.PA,
  ),
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
