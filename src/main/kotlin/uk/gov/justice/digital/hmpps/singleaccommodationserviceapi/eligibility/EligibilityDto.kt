package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus

data class EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
  val caseStatus: CaseStatus? = null,
  val caseActions: List<String>? = null,
  val cas2Hdc: ServiceResult? = null,
  val cas2PrisonBail: ServiceResult? = null,
  val cas2CourtBail: ServiceResult? = null,
  val cas3: ServiceResult? = null,
)
