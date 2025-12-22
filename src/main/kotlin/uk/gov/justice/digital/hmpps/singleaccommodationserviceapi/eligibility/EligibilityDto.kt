package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.CaseStatus

data class EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
  val caseStatus: CaseStatus?,
  val caseActions: List<String>,
  val cas2Hdc: ServiceResult?,
  val cas2PrisonBail: ServiceResult?,
  val cas2CourtBail: ServiceResult?,
  val cas3: ServiceResult?,
)
