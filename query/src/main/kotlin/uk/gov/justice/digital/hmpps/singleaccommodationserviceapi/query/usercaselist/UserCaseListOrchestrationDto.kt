package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.usercaselist

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess

data class UserCaseListOrchestrationDto(
  val caseAccess: List<CaseAccess>,
  val caseSummaries: List<CaseSummary>,
)
