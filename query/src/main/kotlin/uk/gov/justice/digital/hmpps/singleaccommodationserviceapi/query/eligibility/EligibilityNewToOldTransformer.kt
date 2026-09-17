package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CrsServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PaServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew

object EligibilityNewToOldTransformer {

  fun toServiceStatusOld(
    serviceStatus: ServiceStatusNew,
  ) = when (serviceStatus) {
    ServiceStatusNew.CAS1_APPLICATION_REJECTED -> ServiceStatus.APPLICATION_REJECTED
    ServiceStatusNew.CAS1_ARRIVED -> ServiceStatus.ARRIVED
    ServiceStatusNew.CAS1_INFO_REQUESTED -> ServiceStatus.INFO_REQUESTED
    ServiceStatusNew.CAS1_NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    ServiceStatusNew.CAS1_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.CAS1_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.CAS1_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusNew.CAS1_PLACEMENT_BOOKED -> ServiceStatus.PLACEMENT_BOOKED
    ServiceStatusNew.CAS1_PLACEMENT_CANCELLED -> ServiceStatus.PLACEMENT_CANCELLED
    ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED -> ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED
    ServiceStatusNew.CAS1_PLACEMENT_REQUEST_REJECTED -> ServiceStatus.PLACEMENT_REQUEST_REJECTED
    ServiceStatusNew.CAS1_PLACEMENT_REQUEST_SUBMITTED -> ServiceStatus.PLACEMENT_REQUEST_SUBMITTED
    ServiceStatusNew.CAS1_PLACEMENT_REQUEST_WITHDRAWN -> ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN
    ServiceStatusNew.CAS1_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusNew.CAS1_UPCOMING -> ServiceStatus.UPCOMING

    ServiceStatusNew.CAS2_AWAITING_ARRIVAL -> ServiceStatus.AWAITING_ARRIVAL
    ServiceStatusNew.CAS2_AWAITING_DECISION -> ServiceStatus.AWAITING_DECISION
    ServiceStatusNew.CAS2_CANCELLED -> ServiceStatus.CANCELLED
    ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED -> ServiceStatus.MORE_INFORMATION_NEEDED
    ServiceStatusNew.CAS2_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.CAS2_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.CAS2_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusNew.CAS2_OFFER_ACCEPTED -> ServiceStatus.OFFER_ACCEPTED
    ServiceStatusNew.CAS2_OFFER_DECLINED_OR_WITHDRAWN -> ServiceStatus.OFFER_DECLINED_OR_WITHDRAWN
    ServiceStatusNew.CAS2_ON_WAITING_LIST -> ServiceStatus.ON_WAITING_LIST
    ServiceStatusNew.CAS2_PLACE_OFFERED -> ServiceStatus.PLACE_OFFERED
    ServiceStatusNew.CAS2_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusNew.CAS2_UNKNOWN -> ServiceStatus.UNKNOWN
    ServiceStatusNew.CAS2_UPCOMING -> ServiceStatus.UPCOMING
    ServiceStatusNew.CAS2_WITHDRAWN -> ServiceStatus.WITHDRAWN

    ServiceStatusNew.CAS3_BEDSPACE_OFFERED -> ServiceStatus.BEDSPACE_OFFERED
    ServiceStatusNew.CAS3_BOOKING_CANCELLED -> ServiceStatus.BOOKING_CANCELLED
    ServiceStatusNew.CAS3_BOOKING_CONFIRMED -> ServiceStatus.BOOKING_CONFIRMED
    ServiceStatusNew.CAS3_CANNOT_START_YET -> ServiceStatus.CANNOT_START_YET
    ServiceStatusNew.CAS3_NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    ServiceStatusNew.CAS3_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.CAS3_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.CAS3_NOT_SUBMITTED -> ServiceStatus.NOT_SUBMITTED
    ServiceStatusNew.CAS3_REJECTED -> ServiceStatus.REJECTED
    ServiceStatusNew.CAS3_SUBMITTED -> ServiceStatus.SUBMITTED

    ServiceStatusNew.CRS_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.CRS_NOT_REQUIRED -> ServiceStatus.NOT_REQUIRED
    ServiceStatusNew.CRS_NOT_STARTED_REFERRAL -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.CRS_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusNew.CRS_UPCOMING_ACCOMMODATION_REFERRAL -> ServiceStatus.UPCOMING
    ServiceStatusNew.CRS_NOT_STARTED_ACCOMMODATION_REFERRAL -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.CRS_UPCOMING_REFERRAL -> ServiceStatus.UPCOMING

    ServiceStatusNew.DTR_ACCEPTED -> ServiceStatus.ACCEPTED
    ServiceStatusNew.DTR_NOT_ACCEPTED -> ServiceStatus.NOT_ACCEPTED
    ServiceStatusNew.DTR_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.DTR_NOT_REQUIRED -> ServiceStatus.NOT_REQUIRED
    ServiceStatusNew.DTR_NOT_STARTED -> ServiceStatus.NOT_STARTED
    ServiceStatusNew.DTR_SUBMITTED -> ServiceStatus.SUBMITTED
    ServiceStatusNew.DTR_UPCOMING -> ServiceStatus.UPCOMING

    ServiceStatusNew.PA_COMPLETED -> ServiceStatus.COMPLETED
    ServiceStatusNew.PA_NOT_ELIGIBLE -> ServiceStatus.NOT_ELIGIBLE
    ServiceStatusNew.PA_NOT_STARTED -> ServiceStatus.NOT_STARTED
  }

  fun toEligibilityDtoOld(
    eligibilityDto: EligibilityDtoNew,
  ) = EligibilityDto(
    crn = eligibilityDto.crn,
    cas1 = Cas1ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas1.serviceResult.serviceStatus),
        action = eligibilityDto.cas1.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.cas1.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.cas1.serviceResult.actionStartDate,
            service = eligibilityDto.cas1.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.cas1.serviceResult.link,
        url = eligibilityDto.cas1.serviceResult.url,
        linkType = eligibilityDto.cas1.serviceResult.linkType,
        failureReasons = eligibilityDto.cas1.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas1.serviceResult.blockingStatusReason,
      ),
      cas1Application = eligibilityDto.cas1.cas1Application,
    ),
    cas2 = Cas2ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas2.serviceResult.serviceStatus),
        action = eligibilityDto.cas2.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.cas2.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.cas2.serviceResult.actionStartDate,
            service = eligibilityDto.cas2.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.cas2.serviceResult.link,
        url = eligibilityDto.cas2.serviceResult.url,
        linkType = eligibilityDto.cas2.serviceResult.linkType,
        failureReasons = eligibilityDto.cas2.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas2.serviceResult.blockingStatusReason,
      ),
      cas2Application = eligibilityDto.cas2.cas2Application,
    ),
    cas3 = Cas3ServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.cas3.serviceResult.serviceStatus),
        action = eligibilityDto.cas3.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.cas3.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.cas3.serviceResult.actionStartDate,
            service = eligibilityDto.cas3.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.cas3.serviceResult.link,
        url = eligibilityDto.cas3.serviceResult.url,
        linkType = eligibilityDto.cas3.serviceResult.linkType,
        failureReasons = eligibilityDto.cas3.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.cas3.serviceResult.blockingStatusReason,
      ),
      cas3Application = eligibilityDto.cas3.cas3Application,
    ),
    dtr = DtrServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.dtr.serviceResult.serviceStatus),
        action = eligibilityDto.dtr.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.dtr.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.dtr.serviceResult.actionStartDate,
            service = eligibilityDto.dtr.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.dtr.serviceResult.link,
        url = eligibilityDto.dtr.serviceResult.url,
        linkType = eligibilityDto.dtr.serviceResult.linkType,
        failureReasons = eligibilityDto.dtr.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.dtr.serviceResult.blockingStatusReason,
      ),
      caseId = eligibilityDto.dtr.caseId,
      submission = eligibilityDto.dtr.submission,
    ),
    crs = CrsServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.crs.serviceResult.serviceStatus),
        action = eligibilityDto.crs.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.crs.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.crs.serviceResult.actionStartDate,
            service = eligibilityDto.crs.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.crs.serviceResult.link,
        url = eligibilityDto.crs.serviceResult.url,
        linkType = eligibilityDto.crs.serviceResult.linkType,
        failureReasons = eligibilityDto.crs.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.crs.serviceResult.blockingStatusReason,
      ),
      commissionedRehabilitativeServices = eligibilityDto.crs.commissionedRehabilitativeServices,
    ),
    pa = PaServiceResult(
      serviceResult = ServiceResult(
        serviceStatus = toServiceStatusOld(eligibilityDto.pa.serviceResult.serviceStatus),
        action = eligibilityDto.pa.serviceResult.serviceStatus.action?.let {
          CaseAction(
            type = eligibilityDto.pa.serviceResult.serviceStatus.action!!,
            startDate = eligibilityDto.pa.serviceResult.actionStartDate,
            service = eligibilityDto.pa.serviceResult.serviceStatus.service,
          )
        },
        link = eligibilityDto.pa.serviceResult.link,
        url = eligibilityDto.pa.serviceResult.url,
        linkType = eligibilityDto.pa.serviceResult.linkType,
        failureReasons = eligibilityDto.pa.serviceResult.failureReasons,
        blockingStatusReason = eligibilityDto.pa.serviceResult.blockingStatusReason,
      ),
    ),
    caseActions = listOf(
      eligibilityDto.dtr,
      eligibilityDto.crs,
      eligibilityDto.cas1,
      eligibilityDto.cas2,
      eligibilityDto.cas3,
      eligibilityDto.pa,
    )
      .filter { it.actionPosition != -1 }
      .sortedWith(compareBy(nullsLast()) { it.actionPosition })
      .map { serviceResultWrapper ->
        CaseAction(
          type = serviceResultWrapper.serviceResult.serviceStatus.action!!,
          startDate = serviceResultWrapper.serviceResult.actionStartDate,
          service = serviceResultWrapper.serviceResult.serviceStatus.service,
        )
      },
  )
}
