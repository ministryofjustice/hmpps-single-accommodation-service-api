package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.http.HttpMethod
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

data class EndpointCacheConfig(
  val enabled: Boolean = true,
  val cacheName: String,
  val ttl: Duration,
  val maxSize: Long? = null,
  val keyStrategy: CacheKeyStrategy,
)

data class EndpointDefinition(
  val name: String,
  val baseUrl: String,
  val path: String,
  val method: HttpMethod = HttpMethod.GET,
  val cacheConfig: EndpointCacheConfig? = null,
  val resilienceConfig: ResilienceConfig? = null,
  val responseType: KClass<*> = String::class,
  val headers: Map<String, String> = emptyMap(),
)

data class ResilienceConfig(
  val timeout: Duration? = null,
  val circuitBreaker: CircuitBreakerConfig? = null,
  val retry: RetryConfig? = null,
)

data class CircuitBreakerConfig(
  val failureRateThreshold: Float = 50.0f,
  val waitDurationInOpenState: Duration = Duration.ofSeconds(10),
  val slidingWindowSize: Int = 10,
  val minimumNumberOfCalls: Int = 5,
  val permittedNumberOfCallsInHalfOpenState: Int = 3,
)

data class RetryConfig(
  val maxAttempts: Int = 3,
  val waitDuration: Duration = Duration.ofMillis(500),
  val retryOnResult: ((Any?) -> Boolean)? = null,
)

data class EndpointError(
  val endpointName: String,
  val message: String,
  val errorType: ErrorType,
  val timestamp: Instant = Instant.now(),
  val cause: String? = null,
) {
  enum class ErrorType {
    TIMEOUT,
    NETWORK_ERROR,
    HTTP_ERROR,
    UNKNOWN_ERROR, // / other error types?
  }
}

sealed class EndpointResult<out T> {
  data class Success<T>(val data: T) : EndpointResult<T>()
  data class Failure(val error: EndpointError) : EndpointResult<Nothing>()

  val isSuccess: Boolean
    get() = this is Success

  val isFailure: Boolean
    get() = this is Failure

  fun getOrNull(): T? = when (this) {
    is Success -> data
    is Failure -> null
  }
}

data class AggregatedResponse(
  val results: Map<String, EndpointResult<*>>,
  val timestamp: Instant = Instant.now(),
  val totalEndpoints: Int = results.size,
  val successfulEndpoints: Int = results.values.count { it.isSuccess },
  val failedEndpoints: Int = results.values.count { it.isFailure },
) {

  inline fun <reified T> getResult(endpointName: String): EndpointResult<T>? = results[endpointName] as? EndpointResult<T>

  @Suppress("UNCHECKED_CAST")
  fun getSuccessfulResults(): Map<String, Any> = results
    .filterValues { it.isSuccess }
    .mapValues { (_, value) ->
      @Suppress("UNCHECKED_CAST")
      (value as EndpointResult.Success<Any>).data
    }

  fun getFailedResults(): Map<String, EndpointError> = results
    .filterValues { it.isFailure }
    .mapValues { (_, value) -> (value as EndpointResult.Failure).error }
}

typealias CacheKeyStrategy = (Map<String, Any?>) -> String

fun defaultCacheKeyStrategy(endpointName: String): CacheKeyStrategy = { params ->
  val sortedParams = params.toSortedMap()
  val paramString = sortedParams.entries
    .joinToString(":") { "${it.key}=${it.value}" }
  "$endpointName:$paramString"
}
