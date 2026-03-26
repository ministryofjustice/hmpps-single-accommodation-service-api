package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

import org.springframework.http.HttpStatus

data class UpstreamFailure(
  val callKey: String,
  val type: FailureType,
  val errorDetail: ErrorDetail,
)

enum class FailureType {
  UPSTREAM_HTTP_ERROR,
  TIMEOUT,
  UNKNOWN_ERROR,
}

data class ErrorDetail(
  val httpStatus: HttpStatus?,
  val message: String,
)

sealed class AggregatorCallOutcome<out T> {
  data class Success<out T>(val data: T) : AggregatorCallOutcome<T>()
  data class Failure(
    val type: FailureType,
    val errorDetail: ErrorDetail,
  ) : AggregatorCallOutcome<Nothing>()
}

inline fun <reified T> Map<String, Any>.getRequiredResult(key: String): T = when (
  val outcome = this[key] as? AggregatorCallOutcome<*>
    ?: error("No result found for $key")
) {
  is AggregatorCallOutcome.Success ->
    outcome.data as? T
      ?: error("$key returned unexpected type: ${outcome.data!!::class.simpleName}")
  is AggregatorCallOutcome.Failure ->
    error("$key failed: ${outcome.errorDetail.message}")
}

inline fun <reified T> Map<String, Any>.getOptionalResult(key: String): T? {
  return when (val outcome = this[key] as? AggregatorCallOutcome<*> ?: return null) {
    is AggregatorCallOutcome.Success -> outcome.data as? T
    is AggregatorCallOutcome.Failure -> null
  }
}

fun Map<String, Any>.extractFailures(): List<UpstreamFailure> = this.mapNotNull { (key, value) ->
  val failure = value as? AggregatorCallOutcome.Failure ?: return@mapNotNull null
  UpstreamFailure(
    callKey = key,
    type = failure.type,
    errorDetail = failure.errorDetail,
  )
}
