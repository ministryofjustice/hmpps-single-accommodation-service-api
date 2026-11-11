package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.usercaselist

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel

data class CaseListFilters(
  val searchTerm: String? = null,
  val riskLevel: RiskLevel? = null,
  val teamCode: String? = null,
  val peopleType: PeopleType? = null,
)
