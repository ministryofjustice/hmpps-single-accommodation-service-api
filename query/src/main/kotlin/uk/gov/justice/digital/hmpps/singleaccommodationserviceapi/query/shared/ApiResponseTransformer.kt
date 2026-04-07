package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure

object ApiResponseTransformer {
  fun <T> toApiResponseDto(
    data: T,
    upstreamFailures: List<UpstreamFailure> = emptyList(),
  ) = ApiResponseDto(
    data = data,
    upstreamFailures = upstreamFailures.map { UpstreamFailureTransformer.toUpstreamFailureDto(it) },
  )
}
