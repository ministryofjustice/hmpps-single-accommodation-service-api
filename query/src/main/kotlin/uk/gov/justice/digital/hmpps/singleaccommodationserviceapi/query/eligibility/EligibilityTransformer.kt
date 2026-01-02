package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

fun toEligibilityDto(crn: String, cas1: ServiceResult) = EligibilityDto(
  crn,
  cas1,
  caseStatus = null,
  caseActions = listOf(),
  cas2Hdc = null,
  cas2PrisonBail = null,
  cas2CourtBail = null,
  cas3 = null,
)
