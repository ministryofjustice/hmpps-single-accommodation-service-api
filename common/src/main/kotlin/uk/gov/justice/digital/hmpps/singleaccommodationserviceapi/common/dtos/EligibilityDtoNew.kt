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
  val blockingStatusReason: BlockingReason? = null,
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
  val blockingStatusReason: BlockingReason? = null,
  val failureReasons: List<FailureReason> = emptyList(),
) {
  fun toResult(actionStartDate: LocalDate? = null) = ServiceResultNew(
    serviceStatus = serviceStatus,
    actionStartDate = actionStartDate,
    url = url,
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
  val service: AccommodationService,
  val proposedAction: CaseActionType? = null,
  val link: Link? = null,
  val isUpcoming: Boolean = false,
) {

  // CAS1 Service Statuses
  CAS1_PLACEMENT_BOOKED(
    service = AccommodationService.CAS1,
    link = Link(
      text = EligibilityKeys.VIEW_APPLICATION,
      type = LinkType.CAS1_VIEW_APPLICATION,
    ),
  ),
  CAS1_UPCOMING(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    isUpcoming = true,
  ),
  CAS1_NOT_STARTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS1_START_APPLICATION),
  ),
  CAS1_NOT_SUBMITTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CONTINUE_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.CONTINUE_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_INFO_REQUESTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.PROVIDE_INFORMATION,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_SUBMITTED(
    service = AccommodationService.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_NOT_ARRIVED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_CANCELLED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_REQUEST_NOT_STARTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_REQUEST_WITHDRAWN(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_REQUEST_SUBMITTED(
    service = AccommodationService.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_PLACEMENT_REQUEST_REJECTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.CREATE_PLACEMENT,
    link = Link(text = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST, type = LinkType.CAS1_VIEW_APPLICATION),
  ),
  CAS1_APPLICATION_REJECTED(
    service = AccommodationService.CAS1,
    proposedAction = CaseActionType.START_APPROVED_PREMISE_APPLICATION,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS1_START_APPLICATION),
  ),
  CAS1_ARRIVED(
    service = AccommodationService.CAS1,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS1_VIEW_APPLICATION),
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
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_SUBMITTED(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_NOT_STARTED_COMMUNITY(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.START_REFERRAL, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_NOT_STARTED_PRISON(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_NOT_STARTED_CAS1(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_NOT_STARTED_CAS2(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.START_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_NOT_SUBMITTED(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.CONTINUE_A_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.CONTINUE_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_UPCOMING(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.START_CAS2_REFERRAL,
    isUpcoming = true,
  ),
  CAS2_MORE_INFORMATION_NEEDED(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_AWAITING_DECISION(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_ON_WAITING_LIST(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_PLACE_OFFERED(
    service = AccommodationService.CAS2,
    proposedAction = CaseActionType.REPLY_TO_CAS2_PLACE_OFFER,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_OFFER_ACCEPTED(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_CANCELLED(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_AWAITING_ARRIVAL(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.VIEW_APPLICATION, type = LinkType.CAS2_VIEW_APPLICATION),
  ),
  CAS2_WITHDRAWN(
    service = AccommodationService.CAS2,
    link = Link(text = EligibilityKeys.START_NEW_APPLICATION, type = LinkType.CAS2_START_APPLICATION),
  ),
  CAS2_NOT_ELIGIBLE(
    service = AccommodationService.CAS2,
  ),

  // CAS3 Service Statuses
  CAS3_NOT_ARRIVED(
    service = AccommodationService.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_SUBMITTED(
    service = AccommodationService.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_NOT_STARTED(
    service = AccommodationService.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
  ),
  CAS3_NOT_STARTED_NEW(
    service = AccommodationService.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_NEW_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
  ),
  CAS3_NOT_SUBMITTED(
    service = AccommodationService.CAS3,
    proposedAction = CaseActionType.CONTINUE_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.CONTINUE_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_REJECTED(
    service = AccommodationService.CAS3,
    proposedAction = CaseActionType.START_CAS3_REFERRAL,
    link = Link(text = EligibilityKeys.START_NEW_REFERRAL, type = LinkType.CAS3_START_REFERRAL),
  ),
  CAS3_BEDSPACE_OFFERED(
    service = AccommodationService.CAS3,
    proposedAction = CaseActionType.REPLY_TO_CAS3_BEDSPACE_OFFER,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_BOOKING_CONFIRMED(
    service = AccommodationService.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_ARRIVED(
    service = AccommodationService.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_BOOKING_CANCELLED(
    service = AccommodationService.CAS3,
    link = Link(text = EligibilityKeys.VIEW_REFERRAL, type = LinkType.CAS3_VIEW_REFERRAL),
  ),
  CAS3_CANNOT_START_YET(
    service = AccommodationService.CAS3,
  ),
  CAS3_NOT_ELIGIBLE(
    service = AccommodationService.CAS3,
  ),

  // CRS Service Statuses
  CRS_NOT_STARTED_REFERRAL(
    service = AccommodationService.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
  ),
  CRS_NOT_STARTED_ACCOMMODATION_REFERRAL(
    service = AccommodationService.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
  ),
  CRS_UPCOMING_ACCOMMODATION_REFERRAL(
    service = AccommodationService.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
    isUpcoming = true,
  ),
  CRS_UPCOMING_REFERRAL(
    service = AccommodationService.CRS,
    proposedAction = CaseActionType.SUBMIT_CRS_REFERRAL,
    isUpcoming = true,
  ),
  CRS_SUBMITTED(
    service = AccommodationService.CRS,
    link = Link(text = EligibilityKeys.VIEW_REFER_AND_MONITOR),
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
    proposedAction = CaseActionType.ADD_DTR_OUTCOME,
    link = Link(text = EligibilityKeys.ADD_OUTCOME),
  ),
  DTR_UPCOMING(
    service = AccommodationService.DTR,
    proposedAction = CaseActionType.SUBMIT_DTR_REFERRAL,
    isUpcoming = true,
  ),
  DTR_NOT_STARTED(
    service = AccommodationService.DTR,
    proposedAction = CaseActionType.ADD_DTR_REFERRAL_DETAILS,
    link = Link(text = EligibilityKeys.ADD_REFERRAL_DETAILS),
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
    proposedAction = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS,
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
