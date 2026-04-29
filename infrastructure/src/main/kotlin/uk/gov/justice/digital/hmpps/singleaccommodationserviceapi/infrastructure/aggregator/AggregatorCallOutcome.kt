package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

import org.springframework.http.HttpStatus

data class UpstreamFailure(
  val callKey: String,
  val type: FailureType,
  val errorDetail: ErrorDetail,
  val identifier: String? = null,
)

enum class FailureType {
  UPSTREAM_HTTP_ERROR,
  TIMEOUT,
  UNKNOWN_ERROR,
}

data class ErrorDetail(
  val httpStatus: HttpStatus? = null,
  val message: String,
)

sealed class AggregatorCallOutcome<out T> {
  data class Success<out T>(val data: T) : AggregatorCallOutcome<T>()
  data class Failure(
    val type: FailureType,
    val errorDetail: ErrorDetail,
    val identifier: String? = null,
  ) : AggregatorCallOutcome<Nothing>()
}

inline fun <reified T> Map<String, Any>.getResult(key: String): T? {
  return when (val outcome = this[key] as? AggregatorCallOutcome<*> ?: return null) {
    is AggregatorCallOutcome.Success -> outcome.data as? T
    is AggregatorCallOutcome.Failure -> null
  }
}

inline fun <reified T> Map<String, Any>.getRequiredResult(key: String): T {
  val outcome = this[key] as? AggregatorCallOutcome<*>
    ?: error("No result for required call '$key'")
  return when (outcome) {
    is AggregatorCallOutcome.Success -> outcome.data as T
    is AggregatorCallOutcome.Failure -> error(
      "Required upstream call '$key' failed: ${outcome.errorDetail.message}",
    )
  }
}

fun Map<String, Any>.getFailures(): List<UpstreamFailure> = this.mapNotNull { (key, value) ->
  val failure = value as? AggregatorCallOutcome.Failure ?: return@mapNotNull null
  UpstreamFailure(
    callKey = key,
    type = failure.type,
    errorDetail = failure.errorDetail,
    identifier = failure.identifier,
  )
}
