package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.output

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityV2Dto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PpcServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2

object EligibilityV2ToOldTransformer {

  fun toServiceStatusOld(
    serviceStatus: ServiceStatusV2,
  ) = when (serviceStatus) {
    ServiceStatusV2.PPC_NOT_STARTED -> ServiceStatus.PPC_NOT_STARTED
    ServiceStatusV2.PPC_NOT_REQUIRED -> ServiceStatus.NOT_REQUIRED
    ServiceStatusV2.PPC_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED -> ServiceStatus.PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED
    ServiceStatusV2.PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED -> ServiceStatus.PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED
    ServiceStatusV2.CAS1_APPLICATION_REJECTED -> ServiceStatus.APPLICATION_REJECTED
    ServiceStatusV2.CAS1_ARRIVED -> ServiceStatus.ARRIVED
    ServiceStatusV2.CAS1_INFO_REQUESTED -> ServiceStatus.INFO_REQUESTED
    ServiceStatusV2.CAS1_NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    ServiceStatusV2.CAS1_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.CAS1_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CAS1_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusV2.CAS1_PLACEMENT_BOOKED -> ServiceStatus.PLACEMENT_BOOKED
    ServiceStatusV2.CAS1_PLACEMENT_CANCELLED -> ServiceStatus.PLACEMENT_CANCELLED
    ServiceStatusV2.CAS1_PLACEMENT_REQUEST_NOT_STARTED -> ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED
    ServiceStatusV2.CAS1_PLACEMENT_REQUEST_REJECTED -> ServiceStatus.PLACEMENT_REQUEST_REJECTED
    ServiceStatusV2.CAS1_PLACEMENT_REQUEST_SUBMITTED -> ServiceStatus.PLACEMENT_REQUEST_SUBMITTED
    ServiceStatusV2.CAS1_PLACEMENT_REQUEST_WITHDRAWN -> ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN
    ServiceStatusV2.CAS1_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusV2.CAS1_UPCOMING -> ServiceStatus.UPCOMING
    ServiceStatusV2.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED -> ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED

    ServiceStatusV2.CAS2_AWAITING_ARRIVAL -> ServiceStatus.AWAITING_ARRIVAL
    ServiceStatusV2.CAS2_AWAITING_DECISION -> ServiceStatus.AWAITING_DECISION
    ServiceStatusV2.CAS2_CANCELLED -> ServiceStatus.CANCELLED
    ServiceStatusV2.CAS2_MORE_INFORMATION_NEEDED -> ServiceStatus.MORE_INFORMATION_NEEDED
    ServiceStatusV2.CAS2_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.CAS2_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CAS2_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusV2.CAS2_OFFER_ACCEPTED -> ServiceStatus.OFFER_ACCEPTED
    ServiceStatusV2.CAS2_OFFER_DECLINED_OR_WITHDRAWN -> ServiceStatus.OFFER_DECLINED_OR_WITHDRAWN
    ServiceStatusV2.CAS2_ON_WAITING_LIST -> ServiceStatus.ON_WAITING_LIST
    ServiceStatusV2.CAS2_PLACE_OFFERED -> ServiceStatus.PLACE_OFFERED
    ServiceStatusV2.CAS2_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusV2.CAS2_UPCOMING -> ServiceStatus.UPCOMING
    ServiceStatusV2.CAS2_WITHDRAWN -> ServiceStatus.WITHDRAWN
    ServiceStatusV2.CAS2_ARRIVED -> ServiceStatus.ARRIVED

    ServiceStatusV2.CAS3_BEDSPACE_OFFERED -> ServiceStatus.BEDSPACE_OFFERED
    ServiceStatusV2.CAS3_BOOKING_CANCELLED -> ServiceStatus.BOOKING_CANCELLED
    ServiceStatusV2.CAS3_BOOKING_CONFIRMED -> ServiceStatus.BOOKING_CONFIRMED
    ServiceStatusV2.CAS3_NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    ServiceStatusV2.CAS3_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.CAS3_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CAS3_NOT_STARTED_NEW -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CAS3_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusV2.CAS3_REJECTED -> ServiceStatus.REJECTED
    ServiceStatusV2.CAS3_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusV2.CAS3_ARRIVED -> ServiceStatus.ARRIVED
    ServiceStatusV2.CAS3_UPCOMING -> ServiceStatus.UPCOMING
    ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION -> ServiceStatus.CANNOT_START_YET
    ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS -> ServiceStatus.CANNOT_START_YET
    ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION -> ServiceStatus.CANNOT_START_YET
    ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_CRS -> ServiceStatus.CANNOT_START_YET
    ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR -> ServiceStatus.CANNOT_START_YET

    ServiceStatusV2.CRS_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.CRS_NOT_REQUIRED -> ServiceStatus.NOT_REQUIRED
    ServiceStatusV2.CRS_NOT_STARTED_REFERRAL -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CRS_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusV2.CRS_UPCOMING_ACCOMMODATION_REFERRAL -> ServiceStatus.UPCOMING
    ServiceStatusV2.CRS_NOT_STARTED_ACCOMMODATION_REFERRAL -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.CRS_UPCOMING_REFERRAL -> ServiceStatus.UPCOMING

    ServiceStatusV2.DTR_ACCEPTED -> ServiceStatus.ACCEPTED
    ServiceStatusV2.DTR_NOT_ACCEPTED -> ServiceStatus.NOT_ACCEPTED
    ServiceStatusV2.DTR_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.DTR_NOT_REQUIRED -> ServiceStatus.NOT_REQUIRED
    ServiceStatusV2.DTR_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusV2.DTR_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusV2.DTR_UPCOMING -> ServiceStatus.UPCOMING

    ServiceStatusV2.PA_COMPLETED -> ServiceStatus.COMPLETED
    ServiceStatusV2.PA_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusV2.PA_NOT_STARTED -> ServiceStatus.NOT_STARTED
  }

  fun toEligibilityDtoOld(
    eligibilityDto: EligibilityV2Dto,
  ) = EligibilityDto(
    crn = eligibilityDto.crn,
    cas1 = Cas1ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas1.serviceResult.serviceStatus),
        action = eligibilityDto.cas1.serviceResult.toCaseAction(),
        link = eligibilityDto.cas1.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.cas1.serviceResult.url,
        linkType = eligibilityDto.cas1.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.cas1.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas1.serviceResult.serviceStatus.blockingStatusReason,
      ),
      cas1Application = eligibilityDto.cas1.cas1Application,
    ),
    cas2 = Cas2ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas2.serviceResult.serviceStatus),
        action = eligibilityDto.cas2.serviceResult.toCaseAction(),
        link = eligibilityDto.cas2.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.cas2.serviceResult.url,
        linkType = eligibilityDto.cas2.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.cas2.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas2.serviceResult.serviceStatus.blockingStatusReason,
      ),
      cas2Application = eligibilityDto.cas2.cas2Application,
    ),
    cas3 = Cas3ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas3.serviceResult.serviceStatus),
        action = eligibilityDto.cas3.serviceResult.toCaseAction(),
        link = eligibilityDto.cas3.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.cas3.serviceResult.url,
        linkType = eligibilityDto.cas3.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.cas3.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas3.serviceResult.serviceStatus.blockingStatusReason,
      ),
      cas3Application = eligibilityDto.cas3.cas3Application,
    ),
    dtr = DtrServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.dtr.serviceResult.serviceStatus),
        action = eligibilityDto.dtr.serviceResult.toCaseAction(),
        link = eligibilityDto.dtr.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.dtr.serviceResult.url,
        linkType = eligibilityDto.dtr.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.dtr.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.dtr.serviceResult.serviceStatus.blockingStatusReason,
      ),
      caseId = eligibilityDto.dtr.caseId,
      submission = eligibilityDto.dtr.submission,
    ),
    crs = CrsServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.crs.serviceResult.serviceStatus),
        action = eligibilityDto.crs.serviceResult.toCaseAction(),
        link = eligibilityDto.crs.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.crs.serviceResult.url,
        linkType = eligibilityDto.crs.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.crs.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.crs.serviceResult.serviceStatus.blockingStatusReason,
      ),
      commissionedRehabilitativeServices = eligibilityDto.crs.commissionedRehabilitativeServices,
    ),
    pa = PaServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.pa.serviceResult.serviceStatus),
        action = eligibilityDto.pa.serviceResult.toCaseAction(),
        link = eligibilityDto.pa.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.pa.serviceResult.url,
        linkType = eligibilityDto.pa.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.pa.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.pa.serviceResult.serviceStatus.blockingStatusReason,
      ),
    ),
    ppc = PpcServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.ppc.serviceResult.serviceStatus),
        action = eligibilityDto.ppc.serviceResult.toCaseAction(),
        link = eligibilityDto.ppc.serviceResult.serviceStatus.link?.text,
        url = eligibilityDto.ppc.serviceResult.url,
        linkType = eligibilityDto.ppc.serviceResult.serviceStatus.link?.type,
        failureReasons = eligibilityDto.ppc.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.ppc.serviceResult.serviceStatus.blockingStatusReason,
      ),
    ),
    caseActions = listOf(
      eligibilityDto.dtr,
      eligibilityDto.crs,
      eligibilityDto.ppc,
      eligibilityDto.cas1,
      eligibilityDto.cas2,
      eligibilityDto.cas3,
      eligibilityDto.pa,
    )
      .filter { it.actionPosition != -1 }
      .sortedWith(compareBy(nullsLast()) { it.actionPosition })
      .map { serviceResultWrapper ->
        serviceResultWrapper.serviceResult.toCaseAction()!!
      },
  )

  fun ServiceResultV2.toCaseAction(): CaseAction? = serviceStatus.proposedAction?.let { action ->
    CaseAction(
      type = action,
      startDate = actionStartDate,
      service = serviceStatus.service,
    )
  }
}
