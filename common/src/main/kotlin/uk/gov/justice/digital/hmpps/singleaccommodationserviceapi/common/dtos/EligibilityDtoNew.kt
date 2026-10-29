package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util.requireXor
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
  val url: String? = null,
  val failureReasons: List<FailureReason> = emptyList(),
) {
  init {
    requireXor(
      serviceStatus.isUpcoming,
      actionStartDate == null,
    ) {
      "Action start date is required for upcoming statuses and forbidden otherwise"
    }
  }
}

data class ServiceResultSpec(
  val serviceStatus: ServiceStatusNew,
  val url: String? = null,
  val failureReasons: List<FailureReason> = emptyList(),
) {
  fun toResult(actionStartDate: LocalDate? = null) = ServiceResultNew(
    serviceStatus = serviceStatus,
    actionStartDate = actionStartDate,
    url = url,
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

data class Link(
  val text: String,
  val type: LinkType? = null,
)

object EligibilityKeys {
  // CAS1/CAS2
  const val CREATE_NEW_PLACEMENT_REQUEST = "Create new placement request" // LINK TEXT
  const val CREATE_PLACEMENT_REQUEST = "Create placement request" // LINK TEXT
  const val START_NEW_APPLICATION = "Start new application" // LINK TEXT
  const val START_APPLICATION = "Start application" // LINK TEXT
  const val CONTINUE_APPLICATION = "Continue application" // LINK TEXT
  const val VIEW_APPLICATION = "View application" // LINK TEXT

  // CAS3
  const val START_REFERRAL = "Start referral" // LINK TEXT
  const val START_NEW_REFERRAL = "Start new referral" // LINK TEXT
  const val VIEW_REFERRAL = "View referral" // LINK TEXT
  const val CONTINUE_REFERRAL = "Continue referral" // LINK TEXT

  // DTR
  const val ADD_REFERRAL_DETAILS = "Add referral details" // LINK TEXT
  const val ADD_OUTCOME = "Add outcome" // LINK TEXT

  // CRS
  const val VIEW_REFER_AND_MONITOR = "View refer and monitor" // LINK TEXT
}

enum class ServiceStatusNew(
  val service: ServiceType,
  val proposedAction: CaseActionType? = null,
  val link: Link? = null,
  val isUpcoming: Boolean = false,
  val blockingStatusReason: BlockingReason? = null,
  val uiStatusName: String,
) {

  // CAS1 Service Statuses
  CAS1_PLACEMENT_BOOKED(
    service = ServiceType.CAS1,
    link = Link(
      text = EligibilityKeys.VIEW_APPLICATION,
      type = LinkType.CAS1_VIEW_APPLICATION,
    ),
    uiStatusName = "Placement Booked",
  ),
  CAS1_UPCOMING(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  CAS1_NOT_STARTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS1_START_APPLICATION),
    uiStatusName = "Not Started",
  ),
  CAS1_NOT_SUBMITTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CONTINUE_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.CONTINUE_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Not Submitted",
  ),
  CAS1_INFO_REQUESTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.PROVIDE_INFORMATION,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Information Requested",
  ),
  CAS1_SUBMITTED(
    service = ServiceType.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Submitted",
  ),
  CAS1_NOT_ARRIVED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Not Arrived",
  ),
  CAS1_PLACEMENT_CANCELLED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Cancelled",
  ),
  CAS1_PLACEMENT_REQUEST_NOT_STARTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Request Not Started",
  ),
  CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Request Not Started",
  ),
  CAS1_PLACEMENT_REQUEST_WITHDRAWN(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Request Withdrawn",
  ),
  CAS1_PLACEMENT_REQUEST_SUBMITTED(
    service = ServiceType.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Request Submitted",
  ),
  CAS1_PLACEMENT_REQUEST_REJECTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Placement Request Rejected",
  ),
  CAS1_APPLICATION_REJECTED(
    service = ServiceType.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS1_START_APPLICATION),
    uiStatusName = "Application Rejected",
  ),
  CAS1_ARRIVED(
    service = ServiceType.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
    uiStatusName = "Arrived",
  ),
  CAS1_NOT_ELIGIBLE(
    service = ServiceType.CAS1,
    uiStatusName = "Not Eligible",
  ),

  // CAS2 Service Statuses
  CAS2_UNKNOWN(
    service = ServiceType.CAS2,
    uiStatusName = "Unknown",
  ),
  CAS2_OFFER_DECLINED_OR_WITHDRAWN(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
    uiStatusName = "Offer Declined or Withdrawn",
  ),
  CAS2_SUBMITTED(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Submitted",
  ),
  CAS2_NOT_STARTED(
    service = ServiceType.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
    uiStatusName = "Not Started",
  ),
  CAS2_NOT_SUBMITTED(
    service = ServiceType.CAS2,
    proposedAction = CaseActionType.CONTINUE_A_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.CONTINUE_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Not Submitted",
  ),
  CAS2_UPCOMING(
    service = ServiceType.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  CAS2_MORE_INFORMATION_NEEDED(
    service = ServiceType.CAS2,
    proposedAction = CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "More Information Needed",
  ),
  CAS2_AWAITING_DECISION(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Awaiting Decision",
  ),
  CAS2_ON_WAITING_LIST(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "On Waiting List",
  ),
  CAS2_PLACE_OFFERED(
    service = ServiceType.CAS2,
    proposedAction = CaseActionType.REPLY_TO_CAS2_PLACE_OFFER,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Place Offered",
  ),
  CAS2_OFFER_ACCEPTED(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Offer Accepted",
  ),
  CAS2_CANCELLED(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
    uiStatusName = "Cancelled",
  ),
  CAS2_AWAITING_ARRIVAL(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Awaiting Arrival",
  ),
  CAS2_ARRIVED(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
    uiStatusName = "Arrived",
  ),
  CAS2_WITHDRAWN(
    service = ServiceType.CAS2,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
    uiStatusName = "Withdrawn",
  ),
  CAS2_NOT_ELIGIBLE(
    service = ServiceType.CAS2,
    uiStatusName = "Not Eligible",
  ),

  // CAS3 Service Statuses
  CAS3_NOT_ARRIVED(
    service = ServiceType.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Not Arrived",
  ),
  CAS3_SUBMITTED(
    service = ServiceType.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Submitted",
  ),
  CAS3_UPCOMING(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  CAS3_NOT_STARTED(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
    uiStatusName = "Not Started",
  ),
  CAS3_NOT_STARTED_NEW(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_NEW_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
    uiStatusName = "Not Started",
  ),
  CAS3_NOT_SUBMITTED(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.CONTINUE_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.CONTINUE_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Not Submitted",
  ),
  CAS3_REJECTED(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_NEW_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
    uiStatusName = "Rejected",
  ),
  CAS3_BEDSPACE_OFFERED(
    service = ServiceType.CAS3,
    proposedAction = CaseActionType.REPLY_TO_CAS3_BEDSPACE_OFFER,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Bedspace Offered",
  ),
  CAS3_BOOKING_CONFIRMED(
    service = ServiceType.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Booking Confirmed",
  ),
  CAS3_ARRIVED(
    service = ServiceType.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Arrived",
  ),
  CAS3_BOOKING_CANCELLED(
    service = ServiceType.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
    uiStatusName = "Booking Cancelled",
  ),
  CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION(
    service = ServiceType.CAS3,
    blockingStatusReason = BlockingReason.SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3,
    uiStatusName = "Cannot Start Yet",
  ),
  CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS(
    service = ServiceType.CAS3,
    blockingStatusReason = BlockingReason.SUBMIT_DTR_AND_CRS_BEFORE_CAS3,
    uiStatusName = "Cannot Start Yet",
  ),
  CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION(
    service = ServiceType.CAS3,
    blockingStatusReason = BlockingReason.SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3,
    uiStatusName = "Cannot Start Yet",
  ),
  CAS3_CANNOT_START_YET_SUBMIT_CRS(
    service = ServiceType.CAS3,
    blockingStatusReason = BlockingReason.SUBMIT_CRS_BEFORE_CAS3,
    uiStatusName = "Cannot Start Yet",
  ),
  CAS3_CANNOT_START_YET_SUBMIT_DTR(
    service = ServiceType.CAS3,
    blockingStatusReason = BlockingReason.SUBMIT_DTR_BEFORE_CAS3,
    uiStatusName = "Cannot Start Yet",
  ),
  CAS3_NOT_ELIGIBLE(
    service = ServiceType.CAS3,
    uiStatusName = "Not Eligible",
  ),

  // CRS Service Statuses
  CRS_NOT_STARTED_REFERRAL(
    service = ServiceType.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
    uiStatusName = "Not Started",
  ),
  CRS_NOT_STARTED_ACCOMMODATION_REFERRAL(
    service = ServiceType.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
    uiStatusName = "Not Started",
  ),
  CRS_UPCOMING_ACCOMMODATION_REFERRAL(
    service = ServiceType.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  CRS_UPCOMING_REFERRAL(
    service = ServiceType.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_REFERRAL,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  CRS_SUBMITTED(
    service = ServiceType.CRS,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
    uiStatusName = "Submitted",
  ),
  CRS_NOT_REQUIRED(
    service = ServiceType.CRS,
    uiStatusName = "Not Required",
  ),
  CRS_NOT_ELIGIBLE(
    service = ServiceType.CRS,
    uiStatusName = "Not Eligible",
  ),

  // DTR Service Statuses
  DTR_SUBMITTED(
    service = ServiceType.DTR,
    proposedAction = CaseActionType.ADD_DTR_OUTCOME,
    link = Link(text = EligibilityKeys.ADD_OUTCOME),
    uiStatusName = "Submitted",
  ),
  DTR_UPCOMING(
    service = ServiceType.DTR,
    proposedAction = CaseActionType.SUBMIT_DTR_REFERRAL,
    isUpcoming = true,
    uiStatusName = "Upcoming",
  ),
  DTR_NOT_STARTED(
    service = ServiceType.DTR,
    proposedAction = CaseActionType.ADD_DTR_REFERRAL_DETAILS,
    link = Link(text = EligibilityKeys.ADD_REFERRAL_DETAILS),
    uiStatusName = "Not Started",
  ),
  DTR_ACCEPTED(
    service = ServiceType.DTR,
    uiStatusName = "Accepted",
  ),
  DTR_NOT_ACCEPTED(
    service = ServiceType.DTR,
    uiStatusName = "Not Accepted",
  ),
  DTR_NOT_REQUIRED(
    service = ServiceType.DTR,
    uiStatusName = "Not Required",
  ),
  DTR_NOT_ELIGIBLE(
    service = ServiceType.DTR,
    uiStatusName = "Not Eligible",
  ),

  // PA Service Statuses
  PA_NOT_STARTED(
    service = ServiceType.PA,
    proposedAction = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS,
    uiStatusName = "Not Started",
  ),
  PA_NOT_ELIGIBLE(
    service = ServiceType.PA,
    uiStatusName = "Not Eligible",
  ),
  PA_COMPLETED(
    service = ServiceType.PA,
    uiStatusName = "Completed",
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
  MALE_NOT_HIGH_RISK_TIER,
  NON_MALE_NOT_HIGH_RISK_TIER,
  NOT_IN_PRISON_OR_CAS1_ACCOMMODATION,
  INVALID_CURRENT_ACCOMMODATION_TYPE,
  INVALID_TIER,
  CRS_NOT_SUBMITTED_MALE,
  CRS_NOT_SUBMITTED_NON_MALE,
  HAS_NEXT_ACCOMMODATION,
  DTR_REFERRAL_EXPIRED,
  SUITABLE_CAS1_APPLICATION,
  SUITABLE_CAS3_APPLICATION,
  IS_SETTLED,
  UNDER_18,
  IN_CAS2_ACCOMMODATION,
  IN_CAS3_ACCOMMODATION,
  IN_COMMUNITY,
  SEXUAL_OFFENCE_REGISTRATIONS_PRESENT,
  IN_PRISON_NO_RELEASE_DATE,
  HAS_NO_ROSH_LEVEL,
}

enum class BlockingReason {
  // CAS3 PREREQUISITES
  SUBMIT_DTR_BEFORE_CAS3,
  SUBMIT_CRS_BEFORE_CAS3,
  SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3,
  SUBMIT_DTR_AND_CRS_BEFORE_CAS3,
  SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3,
}

enum class ServiceType {
  CAS1,
  CAS2,
  CAS3,
  DTR,
  CRS,
  PA,
}
