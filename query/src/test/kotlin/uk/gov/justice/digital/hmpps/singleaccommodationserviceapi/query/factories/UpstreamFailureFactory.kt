package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

fun buildUpstreamFailure(
  callKey: String = ApiCallKeys.GET_CASE_SUMMARY,
  type: FailureType = FailureType.UPSTREAM_HTTP_ERROR,
  errorDetail: ErrorDetail = ErrorDetail(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, message = "Upstream error"),
  identifier: String? = null,
) = UpstreamFailure(
  callKey = callKey,
  type = type,
  errorDetail = errorDetail,
  identifier = identifier,
)
