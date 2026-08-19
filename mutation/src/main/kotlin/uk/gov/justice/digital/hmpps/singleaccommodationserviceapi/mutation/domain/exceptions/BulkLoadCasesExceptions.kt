package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

const val TEAM_CODES_REQUIRED_KEY = "teamCodesRequired"

class TeamCodesRequiredException : DomainException(TEAM_CODES_REQUIRED_KEY)
