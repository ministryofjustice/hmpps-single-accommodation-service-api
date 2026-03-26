package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class UpstreamFailureDto(
  val endpoint: String,
  val failureType: UpstreamFailureType,
  val httpResponseStatus: Int?,
  val message: String,
)

enum class UpstreamFailureType {
  UPSTREAM_HTTP_ERROR,
  TIMEOUT,
  UNKNOWN_ERROR,
}
