package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

sealed class DomainException(errorKey: String) : RuntimeException(errorKey)
