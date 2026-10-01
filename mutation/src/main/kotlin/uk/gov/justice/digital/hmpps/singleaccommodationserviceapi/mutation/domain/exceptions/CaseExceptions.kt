package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

const val INVALID_CRNS_KEY = "invalidCrns"

class InvalidCrnsException(crns: List<String>) : DomainException("$INVALID_CRNS_KEY: ${crns.joinToString()}")
