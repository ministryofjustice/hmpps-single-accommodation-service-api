package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure

object UpstreamFailureTransformer {
  fun toUpstreamFailureDto(failure: UpstreamFailure) = UpstreamFailureDto(
    endpoint = failure.callKey,
    failureType = UpstreamFailureType.valueOf(failure.type.name),
    httpResponseStatus = failure.errorDetail.httpStatus,
    message = failure.errorDetail.message,
  )
}
