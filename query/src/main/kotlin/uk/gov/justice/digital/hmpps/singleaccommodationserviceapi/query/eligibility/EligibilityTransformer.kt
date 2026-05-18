package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CommissionedRehabilitativeServicesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
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
  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResult,
    cas3: ServiceResult,
    dtr: ServiceResult,
    crs: ServiceResult,
    pa: ServiceResult,
    data: DomainData,
  ) = EligibilityDto(
    crn = crn,
    cas1 = Cas1ServiceResult(
      serviceResult = cas1,
      cas1Application = toCas1ApplicationDto(data.cas1Application),
    ),
    cas3 = Cas3ServiceResult(
      serviceResult = cas3,
      cas3Application = toCas3ApplicationDto(data.cas3Application),
    ),
    dtr = DtrServiceResult(
      serviceResult = dtr,
      caseId = data.dutyToRefer?.caseId,
      submission = data.dutyToRefer?.submission,
    ),
    crs = CrsServiceResult(
      serviceResult = crs,
      commissionedRehabilitativeServices = toCommissionedRehabilitativeServicesDto(data.commissionedRehabilitativeServices),
    ),
    pa = PaServiceResult(
      serviceResult = pa,
    ),
    caseActions =
    listOf(
      dtr.action,
      crs.action,
      cas1.action,
      cas3.action,
      pa.action,
    ).mapNotNull { it },
  )

  fun toFailedEligibilityDto(
    crn: String,
  ) = EligibilityDto(
    crn = crn,
    cas1 = Cas1ServiceResult(
      serviceResult = toNotEligibleServiceStatus(),
      cas1Application = null,
    ),
    cas3 = Cas3ServiceResult(
      serviceResult = toNotEligibleServiceStatus(),
      cas3Application = null,
    ),
    dtr = DtrServiceResult(
      serviceResult = toNotEligibleServiceStatus(),
      caseId = null,
      submission = null,
    ),
    crs = CrsServiceResult(
      serviceResult = toNotEligibleServiceStatus(),
      commissionedRehabilitativeServices = null,
    ),
    pa = PaServiceResult(
      serviceResult = toNotEligibleServiceStatus(),
    ),
    caseActions = emptyList(),
  )

  fun toNotEligibleServiceStatus(failureReasons: List<FailureReason> = emptyList()) = ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    failureReasons = failureReasons,
  )

  private fun toCas3ApplicationDto(
    cas3Application: Cas3Application?,
  ) = cas3Application?.let { application ->
    Cas3ApplicationDto(
      id = application.id,
      applicationStatus = toCas3ApplicationStatus(application.applicationStatus),
      assessmentStatus = toCas3AssessmentStatus(application.assessmentStatus),
      bookingStatus = toCas3BookingStatus(application.bookingStatus),
    )
  }

  private fun toCas1ApplicationDto(
    cas1Application: Cas1Application?,
  ) = cas1Application?.let { application ->
    Cas1ApplicationDto(
      id = application.id,
      applicationStatus = toCas1ApplicationStatus(application.applicationStatus),
      requestForPlacementStatus = toCas1RequestForPlacementStatus(application.requestForPlacementStatus),
      placementStatus = toCas1PlacementStatus(application.placementStatus),
    )
  }

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
    CrsReferralStatus.DRAFT -> CrsStatus.DRAFT
    CrsReferralStatus.LIVE -> CrsStatus.LIVE
    CrsReferralStatus.COMPLETED -> CrsStatus.COMPLETED
    CrsReferralStatus.WITHDRAWN -> CrsStatus.WITHDRAWN
  }

  private fun toCas1ApplicationStatus(
    cas1ApplicationStatus: Cas1ApplicationStatusInfra,
  ) = when (cas1ApplicationStatus) {
    Cas1ApplicationStatusInfra.AWAITING_ASSESSMENT -> Cas1ApplicationStatus.AWAITING_ASSESSMENT
    Cas1ApplicationStatusInfra.UNALLOCATED_ASSESSMENT -> Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT
    Cas1ApplicationStatusInfra.ASSESSMENT_IN_PROGRESS -> Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS
    Cas1ApplicationStatusInfra.AWAITING_PLACEMENT -> Cas1ApplicationStatus.AWAITING_PLACEMENT
    Cas1ApplicationStatusInfra.PLACEMENT_ALLOCATED -> Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    Cas1ApplicationStatusInfra.REQUEST_FOR_FURTHER_INFORMATION -> Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION
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
