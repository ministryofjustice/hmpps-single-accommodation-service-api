package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus

object EligibilityTransformer {
  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResult,
    cas3: ServiceResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
  ) = EligibilityDto(
    crn = crn,
    cas1 = cas1,
    cas3 = cas3,
    caseActions =
    listOf(
      cas1.action?.text,
      cas3.action?.text,
    ).mapNotNull { it },
    caseStatus = listOf(
      getCaseStatus(cas1),
      getCaseStatusOrDefault(cas3),
    ).maxWith(compareBy<CaseStatus> { it.caseStatusOrder }),
  )

  private fun getCaseStatusOrDefault(serviceResult: ServiceResult?) = serviceResult?.let { getCaseStatus(serviceResult) } ?: CaseStatus.NO_ACTION_REQUIRED

  private fun getCaseStatus(serviceResult: ServiceResult) = when {
    serviceResult.action == null -> CaseStatus.NO_ACTION_REQUIRED
    serviceResult.action!!.isUpcoming == false -> CaseStatus.ACTION_NEEDED
    else -> CaseStatus.ACTION_UPCOMING
  }
}
