package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto

class UpstreamFailureException(
  val upstreamFailures: List<UpstreamFailureDto>,
) : RuntimeException(upstreamFailures.toFailMessage())

private fun List<UpstreamFailureDto>.toFailMessage() = """failureTypes: ${
  this.map { "[${it.failureType}] - [${it.endpoint}] - [${it.message}]" }
    .joinToString { "\n" }
}"""
