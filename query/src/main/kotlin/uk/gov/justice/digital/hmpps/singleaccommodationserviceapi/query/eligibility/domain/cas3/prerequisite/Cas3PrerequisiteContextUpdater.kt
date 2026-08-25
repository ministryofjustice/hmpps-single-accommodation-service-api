package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BlockingReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3PrerequisiteContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val currentFailureReasons = context.currentResult.failureReasons
    val crsOutstandingMale = FailureReason.CRS_NOT_SUBMITTED_MALE in currentFailureReasons
    val crsOutstandingNonMale = FailureReason.CRS_NOT_SUBMITTED_NON_MALE in currentFailureReasons
    val dtrOutstanding = FailureReason.DTR_REFERRAL_EXPIRED in currentFailureReasons

    val blockingStatusReason = when {
      dtrOutstanding && crsOutstandingMale -> BlockingReason.SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3
      dtrOutstanding && crsOutstandingNonMale -> BlockingReason.SUBMIT_DTR_AND_CRS_BEFORE_CAS3
      crsOutstandingMale -> BlockingReason.SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3
      crsOutstandingNonMale -> BlockingReason.SUBMIT_CRS_BEFORE_CAS3
      else -> BlockingReason.SUBMIT_DTR_BEFORE_CAS3
    }
    return ServiceResult(
      serviceStatus = ServiceStatus.CANNOT_START_YET,
      blockingStatusReason = blockingStatusReason,
      failureReasons = context.currentResult.failureReasons,
    )
  }
}
