package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListOrchestrationDto

fun buildCustomCaseListOrchestrationDto(
  userCaseAccess: UserCaseAccess? = UserCaseAccess(emptyList()),
  caseSummaries: List<CaseSummary> = emptyList(),
) = CustomCaseListOrchestrationDto(
  userCaseAccess = userCaseAccess,
  caseSummaries = caseSummaries,
)
