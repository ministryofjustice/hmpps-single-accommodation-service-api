package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate
import java.util.UUID

data class EligibilityDtoNew(
  val crn: String,
  val cas1: Cas1ServiceResultWrapper,
  val cas2: Cas2ServiceResultWrapper,
  val cas3: Cas3ServiceResultWrapper,
  val dtr: DtrServiceResultWrapper,
  val crs: CrsServiceResultWrapper,
  val pa: PaServiceResultWrapper,
)

data class ServiceResultNew(
  val serviceStatus: ServiceStatusNew,
  val actionStartDate: LocalDate? = null,
  val link: String? = null,
  val url: String? = null,
  val linkType: LinkType? = null,
  val failureReasons: List<FailureReason> = emptyList(),
  val blockingStatusReason: BlockingReason? = null,
) {
  init {
    require(!serviceStatus.isUpcoming || actionStartDate != null)
  }

  init {
    require(serviceStatus.isUpcoming || actionStartDate == null)
  }
}

data class ServiceResultSpec(
  val serviceStatus: ServiceStatusNew,
  val link: String? = null,
  val url: String? = null,
  val linkType: LinkType? = null,
  val blockingStatusReason: BlockingReason? = null,
  val failureReasons: List<FailureReason> = emptyList(),
) {
  fun toResult(actionStartDate: LocalDate? = null) = ServiceResultNew(
    serviceStatus = serviceStatus,
    actionStartDate = actionStartDate,
    link = link,
    url = url,
    linkType = linkType,
    blockingStatusReason = blockingStatusReason,
    failureReasons = failureReasons,
  )
}

interface ServiceResultWrapper {
  val serviceResult: ServiceResultNew
  val actionPosition: Int
}

data class PaServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
) : ServiceResultWrapper

data class DtrServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
  val caseId: UUID?,
  val submission: DtrSubmissionDto?,
) : ServiceResultWrapper

data class Cas1ServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
  val cas1Application: Cas1ApplicationDto?,
) : ServiceResultWrapper

data class Cas2ServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
  val cas2Application: Cas2ApplicationDto?,
) : ServiceResultWrapper

data class Cas3ServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
  val cas3Application: Cas3ApplicationDto?,
) : ServiceResultWrapper

data class CrsServiceResultWrapper(
  override val serviceResult: ServiceResultNew,
  override val actionPosition: Int,
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServicesDto?,
) : ServiceResultWrapper

enum class ServiceStatusNew(
  val service: AccommodationService,
  val action: CaseActionType?,
  val isUpcoming: Boolean = false,
) {

  // CAS1 Service Statuses
  CAS1_PLACEMENT_BOOKED(
    service = AccommodationService.CAS1,
    action = null,
  ),
  CAS1_UPCOMING(
    service = AccommodationService.CAS1,
    action = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    isUpcoming = true,
  ),
  CAS1_NOT_STARTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
  ),
  CAS1_NOT_SUBMITTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.CONTINUE_APPROVED_PREMISE_APPLICATION,
  ),
  CAS1_INFO_REQUESTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.PROVIDE_INFORMATION,
  ),
  CAS1_SUBMITTED(
    service = AccommodationService.CAS1,
    action = null,
  ),
  CAS1_NOT_ARRIVED(
    service = AccommodationService.CAS1,
    action = CaseActionType.CREATE_PLACEMENT,
  ),
  CAS1_PLACEMENT_CANCELLED(
    service = AccommodationService.CAS1,
    action = CaseActionType.CREATE_PLACEMENT,
  ),
  CAS1_PLACEMENT_REQUEST_NOT_STARTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.CREATE_PLACEMENT,
  ),
  CAS1_PLACEMENT_REQUEST_WITHDRAWN(
    service = AccommodationService.CAS1,
    action = CaseActionType.CREATE_PLACEMENT,
  ),
  CAS1_PLACEMENT_REQUEST_SUBMITTED(
    service = AccommodationService.CAS1,
    action = null,
  ),
  CAS1_PLACEMENT_REQUEST_REJECTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.CREATE_PLACEMENT,
  ),
  CAS1_APPLICATION_REJECTED(
    service = AccommodationService.CAS1,
    action = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
  ),
  CAS1_ARRIVED(
    service = AccommodationService.CAS1,
    action = null,
  ),
  CAS1_NOT_ELIGIBLE(
    service = AccommodationService.CAS1,
    action = null,
  ),

  // CAS2 Service Statuses
  CAS2_UNKNOWN(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_OFFER_DECLINED_OR_WITHDRAWN(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_SUBMITTED(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_NOT_STARTED(
    service = AccommodationService.CAS2,
    action = CaseActionType.START_CAS2_REFERRAL,
  ),
  CAS2_NOT_SUBMITTED(
    service = AccommodationService.CAS2,
    action = CaseActionType.CONTINUE_A_CAS2_REFERRAL,
  ),
  CAS2_UPCOMING(
    service = AccommodationService.CAS2,
    action = CaseActionType.START_CAS2_REFERRAL,
    isUpcoming = true,
  ),
  CAS2_MORE_INFORMATION_NEEDED(
    service = AccommodationService.CAS2,
    action = CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL,
  ),
  CAS2_AWAITING_DECISION(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_ON_WAITING_LIST(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_PLACE_OFFERED(
    service = AccommodationService.CAS2,
    action = CaseActionType.REPLY_TO_CAS2_PLACE_OFFER,
  ),
  CAS2_OFFER_ACCEPTED(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_CANCELLED(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_AWAITING_ARRIVAL(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_WITHDRAWN(
    service = AccommodationService.CAS2,
    action = null,
  ),
  CAS2_NOT_ELIGIBLE(
    service = AccommodationService.CAS2,
    action = null,
  ),

  // CAS3 Service Statuses
  CAS3_NOT_ARRIVED(
    service = AccommodationService.CAS3,
    action = null,
  ),
  CAS3_SUBMITTED(
    service = AccommodationService.CAS3,
    action = null,
  ),
  CAS3_NOT_STARTED(
    service = AccommodationService.CAS3,
    action = CaseActionType.START_CAS3_REFERRAL,
  ),
  CAS3_NOT_SUBMITTED(
    service = AccommodationService.CAS3,
    action = CaseActionType.CONTINUE_CAS3_REFERRAL,
  ),
  CAS3_REJECTED(
    service = AccommodationService.CAS3,
    action = CaseActionType.START_CAS3_REFERRAL,
  ),
  CAS3_BEDSPACE_OFFERED(
    service = AccommodationService.CAS3,
    action = CaseActionType.REPLY_TO_CAS3_BEDSPACE_OFFER,
  ),
  CAS3_BOOKING_CONFIRMED(
    service = AccommodationService.CAS3,
    action = null,
  ),
  CAS3_BOOKING_CANCELLED(
    service = AccommodationService.CAS3,
    action = null,
  ),
  CAS3_CANNOT_START_YET(
    service = AccommodationService.CAS3,
    action = null,
  ),
  CAS3_NOT_ELIGIBLE(
    service = AccommodationService.CAS3,
    action = null,
  ),

  // CRS Service Statuses
  CRS_NOT_STARTED_REFERRAL(
    service = AccommodationService.CRS,
    action = CaseActionType.SUBMIT_CRS_REFERRAL,
  ),
  CRS_NOT_STARTED_ACCOMMODATION_REFERRAL(
    service = AccommodationService.CRS,
    action = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
  ),
  CRS_UPCOMING_ACCOMMODATION_REFERRAL(
    service = AccommodationService.CRS,
    action = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
    isUpcoming = true,
  ),
  CRS_UPCOMING_REFERRAL(
    service = AccommodationService.CRS,
    action = CaseActionType.SUBMIT_CRS_REFERRAL,
    isUpcoming = true,
  ),
  CRS_SUBMITTED(
    service = AccommodationService.CRS,
    action = null,
  ),
  CRS_NOT_REQUIRED(
    service = AccommodationService.CRS,
    action = null,
  ),
  CRS_NOT_ELIGIBLE(
    service = AccommodationService.CRS,
    action = null,
  ),

  // DTR Service Statuses
  DTR_SUBMITTED(
    service = AccommodationService.DTR,
    action = CaseActionType.ADD_DTR_OUTCOME,
  ),
  DTR_UPCOMING(
    service = AccommodationService.DTR,
    action = CaseActionType.SUBMIT_DTR_REFERRAL,
    isUpcoming = true,
  ),
  DTR_NOT_STARTED(
    service = AccommodationService.DTR,
    action = CaseActionType.ADD_DTR_REFERRAL_DETAILS,
  ),
  DTR_ACCEPTED(
    service = AccommodationService.DTR,
    action = null,
  ),
  DTR_NOT_ACCEPTED(
    service = AccommodationService.DTR,
    action = null,
  ),
  DTR_NOT_REQUIRED(
    service = AccommodationService.DTR,
    action = null,
  ),
  DTR_NOT_ELIGIBLE(
    service = AccommodationService.DTR,
    action = null,
  ),

  // PA Service Statuses
  PA_NOT_STARTED(
    service = AccommodationService.PA,
    action = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS,
  ),
  PA_NOT_ELIGIBLE(
    service = AccommodationService.PA,
    action = null,
  ),
  PA_COMPLETED(
    service = AccommodationService.PA,
    action = null,
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
