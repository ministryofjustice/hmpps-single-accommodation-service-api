package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus
import kotlin.collections.plus

data class EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
  val cas2Hdc: ServiceResult?,
  val cas2PrisonBail: ServiceResult?,
  val cas2CourtBail: ServiceResult?,
  val cas3: ServiceResult?,
  val caseActions: List<String>,
  val caseStatus: CaseStatus,

) {
  constructor(
    crn: String,
    cas1: ServiceResult,
    cas2Hdc: ServiceResult?,
    cas2PrisonBail: ServiceResult?,
    cas2CourtBail: ServiceResult?,
    cas3: ServiceResult?,
  ) : this(
    crn = crn,
    cas1 = cas1,
    cas2Hdc = cas2Hdc,
    cas2PrisonBail = cas2PrisonBail,
    cas2CourtBail = cas2CourtBail,
    cas3 = cas3,
    caseActions = cas1.actions.toList() +
      getListFromNullableList(cas2Hdc?.actions) +
      getListFromNullableList(cas2CourtBail?.actions) +
      getListFromNullableList(cas2PrisonBail?.actions) +
      getListFromNullableList(cas3?.actions),
    caseStatus = listOf(
      cas1.getCaseStatus(),
      getCaseStatusFromNullableServiceResult(cas2Hdc),
      getCaseStatusFromNullableServiceResult(cas2CourtBail),
      getCaseStatusFromNullableServiceResult(cas2PrisonBail),
      getCaseStatusFromNullableServiceResult(cas3),
    ).maxWith(compareBy<CaseStatus> { CaseStatus.caseStatusOrder[it] }),
  )

  companion object {
    private fun getListFromNullableList(nullableList: List<String>?) = (nullableList ?: emptyList()).toList()
    private fun getCaseStatusFromNullableServiceResult(serviceResult: ServiceResult?) = serviceResult?.let {
      serviceResult.getCaseStatus()
    } ?: CaseStatus.NO_ACTION_NEEDED
  }
}
