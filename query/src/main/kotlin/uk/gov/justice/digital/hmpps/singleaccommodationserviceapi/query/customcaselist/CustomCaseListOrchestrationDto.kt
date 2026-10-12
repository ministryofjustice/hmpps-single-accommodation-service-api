package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess

data class CustomCaseListOrchestrationDto(
  val userCaseAccess: UserCaseAccess?,
  val caseSummaries: List<CaseSummary>,
)
