package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

sealed class DomainException(val errorKey: String, val errorMessage: String? = null) : RuntimeException(errorKey)
