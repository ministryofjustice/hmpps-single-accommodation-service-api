package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.usercaselist.UserCaseListOrchestrationDto

fun buildUserCaseListOrchestrationDto(
  caseAccess: List<CaseAccess> = emptyList(),
  caseSummaries: List<CaseSummary> = emptyList(),
) = UserCaseListOrchestrationDto(
  caseAccess = caseAccess,
  caseSummaries = caseSummaries,
)
