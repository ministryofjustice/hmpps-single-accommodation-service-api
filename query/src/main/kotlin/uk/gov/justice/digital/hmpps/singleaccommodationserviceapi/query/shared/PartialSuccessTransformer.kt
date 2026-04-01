package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PartialSuccessResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure

object PartialSuccessTransformer {
  fun <T> toPartialSuccessDto(
    data: T,
    upstreamFailures: List<UpstreamFailure> = emptyList(),
  ) = PartialSuccessResponseDto(
    data = data,
    upstreamFailures = upstreamFailures.map { UpstreamFailureTransformer.toUpstreamFailureDto(it) },
  )
}
