package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class ApiResponseDto<T>(
  val data: T,
  val upstreamFailures: List<UpstreamFailureDto> = emptyList(),
)
