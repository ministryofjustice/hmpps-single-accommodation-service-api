package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonInclude

data class ApiResponseDto<T>(
  val data: T,
  @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
  val upstreamFailures: List<UpstreamFailureDto> = emptyList(),
)
