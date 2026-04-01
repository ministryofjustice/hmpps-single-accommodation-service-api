package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure

data class OrchestrationResultDto<T>(
  val data: T,
  val upstreamFailures: List<UpstreamFailure> = emptyList(),
)
