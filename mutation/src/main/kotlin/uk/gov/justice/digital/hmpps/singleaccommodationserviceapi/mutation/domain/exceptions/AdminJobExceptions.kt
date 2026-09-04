package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

const val TEAM_CODES_REQUIRED_KEY = "teamCodesRequired"
const val CRNS_REQUIRED_KEY = "crnsRequired"

class TeamCodesRequiredException : DomainException(TEAM_CODES_REQUIRED_KEY)
class CrnsRequiredException : DomainException(CRNS_REQUIRED_KEY)
