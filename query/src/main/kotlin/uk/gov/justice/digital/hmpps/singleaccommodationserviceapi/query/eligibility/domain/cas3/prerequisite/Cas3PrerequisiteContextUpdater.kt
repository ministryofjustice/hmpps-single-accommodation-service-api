package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3PrerequisiteContextUpdater : ContextUpdater() {

  private val crsFailureReasons = setOf(FailureReason.CRS_NOT_SUBMITTED, FailureReason.CRS_EXPIRED)

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val failureReasons = context.currentResult.failureReasons
    val crsOutstanding = failureReasons.any { it in crsFailureReasons }
    val dtrOutstanding = FailureReason.DTR_REFERRAL_EXPIRED in failureReasons

    val isMale = context.data.sex == SexCode.M

    val actionType = when {
      dtrOutstanding && crsOutstanding && isMale -> CaseActionType.SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3
      dtrOutstanding && crsOutstanding -> CaseActionType.SUBMIT_DTR_AND_CRS_BEFORE_CAS3
      crsOutstanding && isMale -> CaseActionType.SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3
      crsOutstanding -> CaseActionType.SUBMIT_CRS_BEFORE_CAS3
      else -> CaseActionType.SUBMIT_DTR_BEFORE_CAS3
    }

    return ServiceResult(
      serviceStatus = ServiceStatus.CANNOT_START_YET,
      action = CaseAction(type = actionType),
    )
  }
}
