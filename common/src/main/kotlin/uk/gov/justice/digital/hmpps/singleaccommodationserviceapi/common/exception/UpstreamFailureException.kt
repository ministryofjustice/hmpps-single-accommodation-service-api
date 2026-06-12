package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto

class UpstreamFailureException(
  val failure: UpstreamFailureDto,
) : RuntimeException(failure.toFailMessage())

private fun UpstreamFailureDto.toFailMessage() = """failureType: ${
  this.let { "[${it.failureType}] - [${it.endpoint}] - [${it.message}]" }
}"""
