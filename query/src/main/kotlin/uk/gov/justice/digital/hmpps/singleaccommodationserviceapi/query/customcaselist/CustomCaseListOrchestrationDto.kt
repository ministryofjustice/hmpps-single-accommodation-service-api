package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess

data class CustomCaseListOrchestrationDto(
  val caseAccess: List<CaseAccess>,
  val caseSummaries: List<CaseSummary>,
)
