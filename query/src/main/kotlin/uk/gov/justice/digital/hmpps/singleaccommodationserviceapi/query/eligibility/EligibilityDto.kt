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
    caseActions = cas1.actions +
      cas2Hdc?.actions.orEmpty() +
      cas2CourtBail?.actions.orEmpty() +
      cas2PrisonBail?.actions.orEmpty() +
      cas3?.actions.orEmpty(),
    caseStatus = listOf(
      cas1.getCaseStatus(),
      cas2Hdc.caseStatusOrDefault(),
      cas2CourtBail.caseStatusOrDefault(),
      cas2PrisonBail.caseStatusOrDefault(),
      cas3.caseStatusOrDefault(),
    ).maxWith(compareBy<CaseStatus> { it.caseStatusOrder }),
  )

  companion object {
    private fun ServiceResult?.caseStatusOrDefault(): CaseStatus = this?.getCaseStatus() ?: CaseStatus.NO_ACTION_NEEDED
  }
}
