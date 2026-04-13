package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import org.springframework.http.HttpStatus

data class UpstreamFailureDto(
  val endpoint: String,
  val failureType: UpstreamFailureType,
  val httpResponseStatus: HttpStatus?,
  val message: String,
  val identifier: FailureIdentifier? = null,
)

data class FailureIdentifier(
  val type: IdentifierType,
  val value: String,
)

enum class IdentifierType {
  CRN,
  PRISON_NUMBER,
}

enum class UpstreamFailureType {
  UPSTREAM_HTTP_ERROR,
  TIMEOUT,
  UNKNOWN_ERROR,
}
