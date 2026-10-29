package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListOrchestrationDto

fun buildCustomCaseListOrchestrationDto(
  caseAccess: List<CaseAccess> = emptyList(),
  caseSummaries: List<CaseSummary> = emptyList(),
) = CustomCaseListOrchestrationDto(
  caseAccess = caseAccess,
  caseSummaries = caseSummaries,
)
