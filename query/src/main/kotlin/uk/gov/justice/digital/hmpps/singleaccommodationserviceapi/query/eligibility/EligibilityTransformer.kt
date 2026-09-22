package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssessmentDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1AssessmentSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementPairDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2SubmittedApplicationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ExternalPreviousBookingCancellationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ExternalPreviousBookingDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3PremisesSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3StaffDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResultWrapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PlacementApplicationDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawPlacementRequestReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementPair
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ExternalPreviousBookingCancellation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3LatestBookingPremisesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PreviousBookingDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus as Cas1ApplicationStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus as Cas1PlacementStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus as Cas1RequestForPlacementStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus as Cas3ApplicationStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus as Cas3AssessmentStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus as Cas3BookingStatusInfra

object EligibilityTransformer {

  private val log = LoggerFactory.getLogger(javaClass)

  fun getServiceResultActionOrder(
    cas1: ServiceResultNew,
    cas2: ServiceResultNew,
    cas3: ServiceResultNew,
    dtr: ServiceResultNew,
    crs: ServiceResultNew,
    pa: ServiceResultNew,
  ) = listOf(dtr, crs, cas1, cas2, cas3, pa)
    .filter { it.serviceStatus.proposedAction != null }
    .sortedWith(compareBy(nullsLast()) { it.actionStartDate })

  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResultNew,
    cas2: ServiceResultNew,
    cas3: ServiceResultNew,
    dtr: ServiceResultNew,
    crs: ServiceResultNew,
    pa: ServiceResultNew,
    data: DomainData,
  ): EligibilityDtoNew {
    val serviceResultActionOrder = getServiceResultActionOrder(cas1, cas2, cas3, dtr, crs, pa)

    return EligibilityDtoNew(
      crn = crn,
      cas1 = Cas1ServiceResultWrapper(
        serviceResult = cas1,
        cas1Application = toCas1ApplicationDto(data.cas1Application),
        actionPosition = serviceResultActionOrder.indexOf(cas1),
      ),
      cas2 = Cas2ServiceResultWrapper(
        serviceResult = cas2,
        cas2Application = toCas2ApplicationDto(data.cas2Application),
        actionPosition = serviceResultActionOrder.indexOf(cas2),
      ),
      cas3 = Cas3ServiceResultWrapper(
        serviceResult = cas3,
        cas3Application = toCas3ApplicationDto(data.cas3Application),
        actionPosition = serviceResultActionOrder.indexOf(cas3),
      ),
      dtr = DtrServiceResultWrapper(
        serviceResult = dtr,
        caseId = data.dutyToRefer?.caseId,
        submission = data.dutyToRefer?.submission?.takeIf { surfacesReferralData(dtr) },
        actionPosition = serviceResultActionOrder.indexOf(dtr),
      ),
      crs = CrsServiceResultWrapper(
        serviceResult = crs,
        commissionedRehabilitativeServices = toCommissionedRehabilitativeServicesDto(data.commissionedRehabilitativeServices)
          ?.takeIf { crs.serviceStatus == ServiceStatusNew.CRS_SUBMITTED },
        actionPosition = serviceResultActionOrder.indexOf(crs),
      ),
      pa = PaServiceResultWrapper(
        serviceResult = pa,
        actionPosition = serviceResultActionOrder.indexOf(pa),
      ),
    )
  }

  fun toFailedEligibilityDto(
    crn: String,
  ) = EligibilityDtoNew(
    crn = crn,
    cas1 = Cas1ServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.CAS1),
      cas1Application = null,
      actionPosition = -1,
    ),
    cas2 = Cas2ServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.CAS2),
      cas2Application = null,
      actionPosition = -1,
    ),
    cas3 = Cas3ServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.CAS3),
      cas3Application = null,
      actionPosition = -1,
    ),
    dtr = DtrServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.DTR),
      caseId = null,
      submission = null,
      actionPosition = -1,
    ),
    crs = CrsServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.CRS),
      commissionedRehabilitativeServices = null,
      actionPosition = -1,
    ),
    pa = PaServiceResultWrapper(
      serviceResult = toNotEligibleServiceStatus(AccommodationService.PA),
      actionPosition = -1,
    ),
  )

  fun toNotEligibleServiceStatus(service: AccommodationService, failureReasons: List<FailureReason> = emptyList()) = ServiceResultNew(
    serviceStatus = when (service) {
      AccommodationService.CAS2 -> ServiceStatusNew.CAS2_NOT_ELIGIBLE
      AccommodationService.CAS3 -> ServiceStatusNew.CAS3_NOT_ELIGIBLE
      AccommodationService.CAS1 -> ServiceStatusNew.CAS1_NOT_ELIGIBLE
      AccommodationService.PA -> ServiceStatusNew.PA_NOT_ELIGIBLE
      AccommodationService.DTR -> ServiceStatusNew.DTR_NOT_ELIGIBLE
      AccommodationService.CRS -> ServiceStatusNew.CRS_NOT_ELIGIBLE
    },
    failureReasons = failureReasons,
  )

  fun toNotRequiredServiceStatus(service: AccommodationService, failureReasons: List<FailureReason> = emptyList()) = ServiceResultNew(
    serviceStatus = when (service) {
      AccommodationService.DTR -> ServiceStatusNew.DTR_NOT_REQUIRED
      AccommodationService.CRS -> ServiceStatusNew.CRS_NOT_REQUIRED
      else -> throw IllegalArgumentException("Unexpected not required service: $service")
    },
    failureReasons = failureReasons,
  )

  // DTR referral data should only be surfaced when a referral exists and has relevant service status to show the data
  private val surfacingStatuses = setOf(
    ServiceStatusNew.DTR_SUBMITTED,
    ServiceStatusNew.DTR_ACCEPTED,
    ServiceStatusNew.DTR_NOT_ACCEPTED,
  )

  private fun surfacesReferralData(result: ServiceResultNew) = result.serviceStatus in surfacingStatuses

  private fun toCas3ApplicationDto(
    cas3Application: Cas3Application?,
  ) = cas3Application?.let { application ->

    val submittedApplication = application.submittedApplication
    val latestBooking = submittedApplication?.latestBooking

    Cas3ApplicationDto(
      id = application.id,
      applicationStatus = toCas3ApplicationStatus(application.applicationStatus),
      assessmentStatus = toCas3AssessmentStatus(submittedApplication?.assessmentStatus),
      bookingStatus = toCas3BookingStatus(latestBooking?.status),
      applicationSubmittedDate = submittedApplication?.submittedDate,
      applicationSubmittedBy = submittedApplication?.submittedBy?.let { toCas3StaffDto(it) },
      applicationRejectedReason = submittedApplication?.assessmentRejectionReason,
      bookingProvisionalOfferSentDate = latestBooking?.provisionalOfferSentDate,
      previousBookings = toPreviousBookings(application.previousBookings),
      premises = toCas3PremisesSummaryDto(application.premises),
      uiUrl = application.uiUrl,
    )
  }

  private fun toCas1PremisesSummaryDto(
    premises: Cas1PremisesSummary?,
  ) = premises?.let { premises ->
    Cas1PremisesSummaryDto(
      startDate = premises.startDate,
      endDate = premises.endDate,
      addressLine1 = premises.addressLine1,
      addressLine2 = premises.addressLine2,
      town = premises.town,
      postcode = premises.postcode,
    )
  }

  private fun toCas3PremisesSummaryDto(
    premises: Cas3LatestBookingPremisesDto?,
  ) = premises?.let { premises ->
    Cas3PremisesSummaryDto(
      startDate = premises.startDate,
      endDate = premises.endDate,
      addressLine1 = premises.addressLine1,
      addressLine2 = premises.addressLine2,
      town = premises.town,
      postcode = premises.postcode,
      name = premises.name,
    )
  }

  private fun toCas2ApplicationDto(
    cas2Application: Cas2Application?,
  ) = cas2Application?.let { application ->
    Cas2ApplicationDto(
      uiUrl = application.uiUrl,
      id = application.id,
      submittedApplication = toCas2SubmittedApplicationSummaryDto(application.submittedApplication),
    )
  }

  private fun toCas1ApplicationDto(
    cas1Application: Cas1Application?,
  ) = cas1Application?.let { application ->
    Cas1ApplicationDto(
      id = application.application.id,
      applicationStatus = toCas1ApplicationStatus(application.application.status),
      requestForPlacementStatus = toCas1RequestForPlacementStatus(application.requestForPlacement?.status),
      placementStatus = toCas1PlacementStatus(application.placement?.status),
      uiUrl = application.uiUrl,
      application = toCas1ApplicationSummaryDto(application.application),
      assessment = toCas1AssessmentSummaryDto(application.assessment),
      requestForPlacement = toRequestForPlacementDto(application.requestForPlacement),
      placement = toPlacementDto(application.placement),
      placementHistory = toPlacementHistory(application.placementHistory),
    )
  }

  private fun toPlacementHistory(
    placementHistory: List<Cas1PlacementPair>,
  ) = placementHistory.map { pair ->
    Cas1PlacementPairDto(
      requestForPlacement = toRequestForPlacementDto(pair.requestForPlacement),
      placement = toPlacementDto(pair.placement),
      dateApplied = pair.dateApplied,
    )
  }

  private fun toPreviousBookings(
    previousBookings: List<Cas3PreviousBookingDto>?,
  ) = previousBookings?.map { booking ->
    Cas3ExternalPreviousBookingDto(
      bookingStatus = toCas3BookingStatus(booking.bookingStatus),
      cancellation = toCancellation(booking.cancellation),
    )
  }

  private fun toCancellation(
    cancellation: Cas3ExternalPreviousBookingCancellation?,
  ) = cancellation?.let {
    Cas3ExternalPreviousBookingCancellationDto(
      cancellationDate = it.cancellationDate,
      cancellationReason = it.cancellationReason,
    )
  }

  private fun toCas1ApplicationSummaryDto(
    application: Cas1ApplicationSummary,
  ) = Cas1ApplicationSummaryDto(
    id = application.id,
    status = toCas1ApplicationStatus(application.status),
    createdAt = application.createdAt,
    createdBy = toCas1StaffDto(application.createdBy)!!,
    submittedAt = application.submittedAt,
    expiresAt = application.expiresAt,
  )

  private fun toCas2SubmittedApplicationSummaryDto(
    submittedApplication: Cas2SubmittedApplicationSummary?,
  ) = submittedApplication?.let {
    Cas2SubmittedApplicationSummaryDto(
      submittedAt = it.submittedAt,
      latestAssessmentStatus = it.latestAssessmentStatus,
    )
  }

  private fun toRequestForPlacementDto(
    requestForPlacement: Cas1RequestForPlacementSummary?,
  ) = requestForPlacement?.let {
    Cas1RequestForPlacementSummaryDto(
      status = toCas1RequestForPlacementStatus(it.status),
      decision = toRequestForPlacementDecision(it.decision),
      rejectionReason = it.rejectionReason,
      submittedBy = toCas1StaffDto(it.submittedBy),
      submittedAt = it.submittedAt,
      withdrawalReason = toWithdrawalReason(it.withdrawalReason),
      withdrawalDate = it.withdrawalDate,
      expectedArrivalDate = it.expectedArrivalDate,
      durationDays = it.durationDays,
    )
  }

  private fun toPlacementDto(
    placement: Cas1PlacementSummary?,
  ) = placement?.let {
    Cas1PlacementSummaryDto(
      status = toCas1PlacementStatus(it.status),
      actualArrivalDate = it.actualArrivalDate,
      actualDepartureDate = it.actualDepartureDate,
      cancellationReason = it.cancellationReason,
      premises = toCas1PremisesSummaryDto(it.premises),
    )
  }

  private fun toCas1AssessmentSummaryDto(
    assessment: Cas1AssessmentSummary?,
  ) = assessment?.let {
    Cas1AssessmentSummaryDto(
      decision = toAssessmentDecision(assessment.decision),
      rejectionRationale = assessment.rejectionRationale,
    )
  }

  private fun toCas1StaffDto(
    staff: Cas1Staff?,
  ) = staff?.let {
    Cas1StaffDto(
      name = staff.name,
      username = staff.username,
      staffCode = staff.staffCode,
    )
  }

  private fun toCas3StaffDto(
    staff: Cas3Staff,
  ) = Cas3StaffDto(
    name = staff.name,
    username = staff.username,
    staffCode = staff.staffCode,
  )

  private fun toCommissionedRehabilitativeServicesDto(
    commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
  ) = commissionedRehabilitativeServices?.let {
    CommissionedRehabilitativeServicesDto(
      status = toCrsStatus(it.status),
      submissionDate = it.sentAt?.toLocalDate(),
    )
  }

  private fun toCrsStatus(
    crsStatus: CrsReferralStatus,
  ) = when (crsStatus) {
    CrsReferralStatus.LIVE -> CrsStatus.LIVE
    CrsReferralStatus.COMPLETED -> CrsStatus.COMPLETED
    CrsReferralStatus.WITHDRAWN -> CrsStatus.WITHDRAWN
  }

  private fun toWithdrawalReason(
    withdrawalReason: String?,
  ) = when (withdrawalReason) {
    "DuplicatePlacementRequest" -> WithdrawPlacementRequestReason.DUPLICATE_PLACEMENT_REQUEST
    "AlternativeProvisionIdentified" -> WithdrawPlacementRequestReason.ALTERNATIVE_PROVISION_IDENTIFIED
    "ChangeInCircumstances" -> WithdrawPlacementRequestReason.CHANGE_IN_CIRCUMSTANCES
    "ChangeInReleaseDecision" -> WithdrawPlacementRequestReason.CHANGE_IN_RELEASE_DECISION
    "NoCapacityDueToLostBed" -> WithdrawPlacementRequestReason.NO_CAPACITY_DUE_TO_LOST_BED
    "NoCapacityDueToPlacementPrioritisation" -> WithdrawPlacementRequestReason.NO_CAPACITY_DUE_TO_PLACEMENT_PRIORITISATION
    "NoCapacity" -> WithdrawPlacementRequestReason.NO_CAPACITY
    "ErrorInPlacementRequest" -> WithdrawPlacementRequestReason.ERROR_IN_PLACEMENT_REQUEST
    "WithdrawnByPP" -> WithdrawPlacementRequestReason.WITHDRAWN_BY_PP
    "RelatedApplicationWithdrawn" -> WithdrawPlacementRequestReason.RELATED_APPLICATION_WITHDRAWN
    "RelatedPlacementRequestWithdrawn" -> WithdrawPlacementRequestReason.RELATED_PLACEMENT_REQUEST_WITHDRAWN
    "RelatedPlacementApplicationWithdrawn" -> WithdrawPlacementRequestReason.RELATED_PLACEMENT_APPLICATION_WITHDRAWN
    null -> null
    else -> {
      log.error("Unexpected withdrawal reason: $withdrawalReason")
      null
    }
  }

  private fun toRequestForPlacementDecision(
    decision: String?,
  ) = when (decision) {
    "accepted" -> PlacementApplicationDecision.ACCEPTED
    "rejected" -> PlacementApplicationDecision.REJECTED
    "withdraw" -> PlacementApplicationDecision.WITHDRAW
    "withdrawnByPp" -> PlacementApplicationDecision.WITHDRAWN_BY_PP
    null -> null
    else -> {
      log.error("Unexpected placement application decision: $decision")
      null
    }
  }

  private fun toAssessmentDecision(
    decision: String?,
  ) = when (decision) {
    null -> null

    "accepted" -> AssessmentDecision.ACCEPTED

    "rejected" -> AssessmentDecision.REJECTED

    else -> {
      log.error("Unexpected assessment decision: $decision")
      null
    }
  }

  private fun toCas1ApplicationStatus(
    cas1ApplicationStatus: Cas1ApplicationStatusInfra,
  ) = when (cas1ApplicationStatus) {
    Cas1ApplicationStatusInfra.AWAITING_ASSESSMENT -> Cas1ApplicationStatus.AWAITING_ASSESSMENT
    Cas1ApplicationStatusInfra.UNALLOCATED_ASSESSMENT -> Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT
    Cas1ApplicationStatusInfra.ASSESSMENT_IN_PROGRESS -> Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS
    Cas1ApplicationStatusInfra.AWAITING_PLACEMENT -> Cas1ApplicationStatus.AWAITING_PLACEMENT
    Cas1ApplicationStatusInfra.PLACEMENT_ALLOCATED -> Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    Cas1ApplicationStatusInfra.REQUESTED_FURTHER_INFORMATION -> Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION
    Cas1ApplicationStatusInfra.PENDING_PLACEMENT_REQUEST -> Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST
    Cas1ApplicationStatusInfra.STARTED -> Cas1ApplicationStatus.STARTED
    Cas1ApplicationStatusInfra.REJECTED -> Cas1ApplicationStatus.REJECTED
    Cas1ApplicationStatusInfra.INAPPLICABLE -> Cas1ApplicationStatus.INAPPLICABLE
    Cas1ApplicationStatusInfra.WITHDRAWN -> Cas1ApplicationStatus.WITHDRAWN
    Cas1ApplicationStatusInfra.EXPIRED -> Cas1ApplicationStatus.EXPIRED
  }

  private fun toCas1RequestForPlacementStatus(
    cas1RequestForPlacementStatus: Cas1RequestForPlacementStatusInfra?,
  ) = when (cas1RequestForPlacementStatus) {
    Cas1RequestForPlacementStatusInfra.REQUEST_UNSUBMITTED -> Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED
    Cas1RequestForPlacementStatusInfra.REQUEST_REJECTED -> Cas1RequestForPlacementStatus.REQUEST_REJECTED
    Cas1RequestForPlacementStatusInfra.REQUEST_SUBMITTED -> Cas1RequestForPlacementStatus.REQUEST_SUBMITTED
    Cas1RequestForPlacementStatusInfra.AWAITING_MATCH -> Cas1RequestForPlacementStatus.AWAITING_MATCH
    Cas1RequestForPlacementStatusInfra.REQUEST_WITHDRAWN -> Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN
    Cas1RequestForPlacementStatusInfra.PLACEMENT_BOOKED -> Cas1RequestForPlacementStatus.PLACEMENT_BOOKED
    null -> null
  }

  private fun toCas1PlacementStatus(
    cas1PlacementStatus: Cas1PlacementStatusInfra?,
  ) = when (cas1PlacementStatus) {
    Cas1PlacementStatusInfra.ARRIVED -> Cas1PlacementStatus.ARRIVED
    Cas1PlacementStatusInfra.UPCOMING -> Cas1PlacementStatus.UPCOMING
    Cas1PlacementStatusInfra.DEPARTED -> Cas1PlacementStatus.DEPARTED
    Cas1PlacementStatusInfra.NOT_ARRIVED -> Cas1PlacementStatus.NOT_ARRIVED
    Cas1PlacementStatusInfra.CANCELLED -> Cas1PlacementStatus.CANCELLED
    null -> null
  }

  private fun toCas3BookingStatus(
    cas3BookingStatus: Cas3BookingStatusInfra?,
  ) = when (cas3BookingStatus) {
    Cas3BookingStatusInfra.PROVISIONAL -> Cas3BookingStatus.PROVISIONAL
    Cas3BookingStatusInfra.CONFIRMED -> Cas3BookingStatus.CONFIRMED
    Cas3BookingStatusInfra.ARRIVED -> Cas3BookingStatus.ARRIVED
    Cas3BookingStatusInfra.NOT_MINUS_ARRIVED -> Cas3BookingStatus.NOT_MINUS_ARRIVED
    Cas3BookingStatusInfra.DEPARTED -> Cas3BookingStatus.DEPARTED
    Cas3BookingStatusInfra.CANCELLED -> Cas3BookingStatus.CANCELLED
    Cas3BookingStatusInfra.CLOSED -> Cas3BookingStatus.CLOSED
    null -> null
  }

  private fun toCas3ApplicationStatus(
    cas3ApplicationStatus: Cas3ApplicationStatusInfra,
  ) = when (cas3ApplicationStatus) {
    Cas3ApplicationStatusInfra.IN_PROGRESS -> Cas3ApplicationStatus.IN_PROGRESS
    Cas3ApplicationStatusInfra.SUBMITTED -> Cas3ApplicationStatus.SUBMITTED
    Cas3ApplicationStatusInfra.REQUESTED_FURTHER_INFORMATION -> Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION
    Cas3ApplicationStatusInfra.REJECTED -> Cas3ApplicationStatus.REJECTED
  }

  private fun toCas3AssessmentStatus(
    cas3AssessmentStatus: Cas3AssessmentStatusInfra?,
  ) = when (cas3AssessmentStatus) {
    Cas3AssessmentStatusInfra.UNALLOCATED -> Cas3AssessmentStatus.UNALLOCATED
    Cas3AssessmentStatusInfra.IN_REVIEW -> Cas3AssessmentStatus.IN_REVIEW
    Cas3AssessmentStatusInfra.READY_TO_PLACE -> Cas3AssessmentStatus.READY_TO_PLACE
    Cas3AssessmentStatusInfra.CLOSED -> Cas3AssessmentStatus.CLOSED
    Cas3AssessmentStatusInfra.REJECTED -> Cas3AssessmentStatus.REJECTED
    null -> null
  }
}
