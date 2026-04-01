package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class PartialSuccessResponseDto<T>(
  val data: T,
  val upstreamFailures: List<UpstreamFailureDto> = emptyList(),
)
