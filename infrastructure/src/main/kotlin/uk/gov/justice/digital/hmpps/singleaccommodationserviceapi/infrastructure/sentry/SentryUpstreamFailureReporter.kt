package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorCallOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureReporter

@Service
class SentryUpstreamFailureReporter : UpstreamFailureReporter {
  override fun report(callKey: String, failure: AggregatorCallOutcome.Failure, cause: Exception) {
    if (!shouldReport(failure)) {
      return
    }

    log.debug("Capturing upstream failure in Sentry [callKey={}, failureType={}]", callKey, failure.type)
    Sentry.captureException(cause) { scope ->
      scope.setTag("upstream.call_key", callKey)
      scope.setTag("upstream.failure_type", failure.type.name)
      failure.errorDetail.httpStatus?.let { scope.setTag("upstream.http_status", it.value().toString()) }
      scope.setTag("upstream.identifier_present", (failure.identifier != null).toString())
      scope.setFingerprint(
        listOf(
          "upstream-failure",
          callKey,
          failure.type.name,
          failure.errorDetail.httpStatus?.value()?.toString() ?: "no-status",
        ),
      )
    }
  }

  private fun shouldReport(failure: AggregatorCallOutcome.Failure) = when (failure.type) {
    FailureType.UNKNOWN_ERROR, FailureType.TIMEOUT -> true
    FailureType.UPSTREAM_HTTP_ERROR ->
      failure.errorDetail.httpStatus?.is5xxServerError == true ||
        failure.errorDetail.httpStatus == HttpStatus.TOO_MANY_REQUESTS
  }

  private companion object {
    private val log = LoggerFactory.getLogger(SentryUpstreamFailureReporter::class.java)
  }
}
