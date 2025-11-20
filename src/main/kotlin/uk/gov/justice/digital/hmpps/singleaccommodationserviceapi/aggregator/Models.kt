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

enum class ErrorType {
  NETWORK,
  TIMEOUT,
  CIRCUIT_BREAKER,
  VALIDATION,
  CACHE,
  AUTH,
  RETRY_EXHAUSTED,
  UNKNOWN
}

enum class CircuitBreakerState {
  CLOSED,
  OPEN,
  HALF_OPEN
}

data class CallMeta(
  val endpoint: String,
  val fromCache: Boolean,
  val duration: Long, // milliseconds
  val timestamp: Long, // milliseconds since epoch
  val retries: Int? = null,
  val circuitBreakerState: CircuitBreakerState? = null,
  val attempt: Int? = null,
  val cacheId: String? = null
)

data class ErrorInfo(
  val type: ErrorType,
  val message: String,
  val endpoint: String? = null,
  val originalError: Throwable? = null
)

sealed class CallResult<out T> {
  data class Success<out T>(
    val data: T,
    val meta: CallMeta
  ) : CallResult<T>()

  data class Error(
    val error: ErrorInfo,
    val meta: CallMeta
  ) : CallResult<Nothing>()

  val success: Boolean
    get() = this is Success

  inline fun <R> fold(
    onSuccess: (T, CallMeta) -> R,
    onError: (ErrorInfo, CallMeta) -> R
  ): R = when (this) {
    is Success -> onSuccess(data, meta)
    is Error -> onError(error, meta)
  }
}

data class AggregatedResponse(
  val data: Map<String, Any>,
  val meta: Map<String, CallMeta>,
  val errors: Map<String, ErrorInfo>? = null
)

typealias CacheKeyStrategy = (Map<String, Any?>) -> String

fun defaultCacheKeyStrategy(endpointName: String): CacheKeyStrategy = { params ->
  val sortedParams = params.toSortedMap()
  val paramString = sortedParams.entries
    .joinToString(":") { "${it.key}=${it.value}" }
  "$endpointName:$paramString"
}
