package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseListOrchestrationDto

fun buildCaseListOrchestrationDto(
  cases: List<Case> = emptyList(),
  caseAccess: List<CaseAccess> = cases.map { CaseAccess(crn = it.crn, userExcluded = false, userRestricted = false) },
) = CaseListOrchestrationDto(
  cases = cases,
  caseAccess = caseAccess,
)
