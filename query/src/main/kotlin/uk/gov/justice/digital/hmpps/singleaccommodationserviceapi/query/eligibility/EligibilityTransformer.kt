package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import kotlin.collections.orEmpty

  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResult,
    cas2Hdc: ServiceResult? = null,
    cas2PrisonBail: ServiceResult? = null,
    cas2CourtBail: ServiceResult? = null,
    cas3: ServiceResult? = null,
  ) = EligibilityDto(
    crn = crn,
    cas1 = cas1,
    cas2Hdc = cas2Hdc,
    cas2PrisonBail = cas2PrisonBail,
    cas2CourtBail = cas2CourtBail,
    cas3 = cas3,
    caseActions = cas1.actions.map {it.text} +
      cas2Hdc?.actions.orEmpty().map {it.text} +
      cas2CourtBail?.actions.orEmpty().map {it.text} +
      cas2PrisonBail?.actions.orEmpty().map {it.text} +
      cas3?.actions.orEmpty().map {it.text},
    caseStatus = listOf(
      getCaseStatus(cas1),
      getCaseStatusOrDefault(cas2Hdc),
      getCaseStatusOrDefault(cas2CourtBail),
      getCaseStatusOrDefault(cas2PrisonBail),
      getCaseStatusOrDefault(cas3),
    ).maxWith(compareBy<CaseStatus> { it.caseStatusOrder }),
  )

  private fun getCaseStatusOrDefault(serviceResult: ServiceResult?) = serviceResult?.let { getCaseStatus(serviceResult) } ?: CaseStatus.NO_ACTION_NEEDED

  private fun getCaseStatus(serviceResult: ServiceResult) = if(serviceResult.actions == emptyList<String>()) {
    CaseStatus.NO_ACTION_NEEDED
  } else if (serviceResult.actions.any{ it.isUpcoming == false}) {
    CaseStatus.ACTION_NEEDED
  } else {
    CaseStatus.ACTION_UPCOMING
  }


