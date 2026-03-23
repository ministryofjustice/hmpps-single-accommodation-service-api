package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class UpstreamFailureDto(
  val service: String,
  val type: UpstreamFailureType,
  val httpStatus: Int?,
  val message: String,
)

enum class UpstreamFailureType {
  UPSTREAM_HTTP_ERROR,
  TIMEOUT,
  UNKNOWN_ERROR,
}
