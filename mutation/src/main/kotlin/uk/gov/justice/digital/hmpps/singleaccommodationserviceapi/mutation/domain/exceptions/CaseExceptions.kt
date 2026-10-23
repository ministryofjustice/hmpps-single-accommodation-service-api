package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

const val INVALID_CRNS_KEY = "invalidCrns"

class InvalidCrnsException(val crns: List<String>) :
  DomainException(
    errorKey = INVALID_CRNS_KEY,
    userMessage = "Invalid CRNS: ${crns.joinToString()}",
  )
