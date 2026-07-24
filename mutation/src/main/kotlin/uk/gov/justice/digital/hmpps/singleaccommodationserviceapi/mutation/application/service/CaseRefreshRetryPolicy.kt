package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import java.time.Duration
import java.time.Instant

@Component
@ConfigurationProperties(prefix = "case-refresh.worker")
class CaseRefreshProperties(
  var maxRequestsPerRun: Int = 10,
  var maxAttempts: Int = 3,
  var initialBackoff: Duration = Duration.ofMinutes(1),
  var maxBackoff: Duration = Duration.ofMinutes(30),
  var abandonedClaimTimeout: Duration = Duration.ofMinutes(10),
)

data class CaseRefreshFailure(
  val category: CaseRefreshFailureCategory,
  val detail: String,
)

sealed interface CaseRefreshRetryDecision {
  data class RetryAt(val nextAttemptAt: Instant) : CaseRefreshRetryDecision
  data object FailPermanently : CaseRefreshRetryDecision
}

@Component
class CaseRefreshRetryPolicy(
  private val properties: CaseRefreshProperties,
) {
  fun decide(
    failure: CaseRefreshFailure,
    previousAttempts: Int,
    failedAt: Instant,
  ): CaseRefreshRetryDecision {
    if (!failure.category.isRetryable() || previousAttempts + 1 >= properties.maxAttempts) {
      return CaseRefreshRetryDecision.FailPermanently
    }

    val multiplier = 1L shl previousAttempts.coerceIn(0, MAX_BACKOFF_EXPONENT)
    val calculatedBackoff = properties.initialBackoff.multipliedBy(multiplier)
    val backoff = minOf(calculatedBackoff, properties.maxBackoff)
    return CaseRefreshRetryDecision.RetryAt(failedAt.plus(backoff))
  }

  private companion object {
    const val MAX_BACKOFF_EXPONENT = 30
  }
}

@Component
class CaseRefreshFailureClassifier {
  fun classify(failures: List<UpstreamFailure>): CaseRefreshFailure {
    require(failures.isNotEmpty()) { "At least one upstream failure is required" }

    val classifiedFailures = failures.map(::classify)
    val selectedFailure = classifiedFailures.firstOrNull { !it.category.isRetryable() }
      ?: classifiedFailures.first()

    return selectedFailure.copy(
      detail = failures.joinToString(separator = "; ") {
        "${it.callKey}: ${it.errorDetail.message}"
      }.take(MAX_FAILURE_DETAIL_LENGTH),
    )
  }

  fun unexpected(exception: Exception): CaseRefreshFailure = CaseRefreshFailure(
    category = CaseRefreshFailureCategory.UNEXPECTED_ERROR,
    detail = (exception.message ?: exception::class.simpleName ?: "Unexpected error")
      .take(MAX_FAILURE_DETAIL_LENGTH),
  )

  private fun classify(failure: UpstreamFailure): CaseRefreshFailure = when (failure.type) {
    FailureType.TIMEOUT -> CaseRefreshFailure(
      CaseRefreshFailureCategory.UPSTREAM_TIMEOUT,
      failure.errorDetail.message,
    )
    FailureType.UNKNOWN_ERROR -> CaseRefreshFailure(
      CaseRefreshFailureCategory.UPSTREAM_UNEXPECTED_ERROR,
      failure.errorDetail.message,
    )
    FailureType.UPSTREAM_HTTP_ERROR -> classifyHttpFailure(failure)
  }

  private fun classifyHttpFailure(failure: UpstreamFailure): CaseRefreshFailure {
    val status = failure.errorDetail.httpStatus
    val category = when {
      status == HttpStatus.NOT_FOUND && failure.callKey == ApiCallKeys.GET_TIER ->
        CaseRefreshFailureCategory.CURRENT_TIER_NOT_FOUND
      status?.is4xxClientError == true -> CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR
      status?.is5xxServerError == true -> CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR
      status == null -> CaseRefreshFailureCategory.UPSTREAM_UNEXPECTED_ERROR
      else -> CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR
    }
    return CaseRefreshFailure(
      category = category,
      detail = failure.errorDetail.message,
    )
  }

  private companion object {
    const val MAX_FAILURE_DETAIL_LENGTH = 2_000
  }
}

private fun CaseRefreshFailureCategory.isRetryable(): Boolean = when (this) {
  CaseRefreshFailureCategory.CURRENT_TIER_NOT_FOUND,
  CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR,
  -> false
  CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
  CaseRefreshFailureCategory.UPSTREAM_TIMEOUT,
  CaseRefreshFailureCategory.UPSTREAM_UNEXPECTED_ERROR,
  CaseRefreshFailureCategory.UNEXPECTED_ERROR,
  -> true
}
