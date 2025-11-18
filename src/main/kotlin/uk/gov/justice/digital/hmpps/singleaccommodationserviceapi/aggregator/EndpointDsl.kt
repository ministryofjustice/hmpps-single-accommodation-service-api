package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.http.HttpMethod
import java.time.Duration
import kotlin.reflect.KClass

class EndpointDslBuilder(
  private val name: String,
  private val registry: EndpointRegistry,
) {
  var baseUrl: String = ""
  var path: String = ""
  var method: HttpMethod = HttpMethod.GET
  var responseType: KClass<*> = String::class
  var headers: MutableMap<String, String> = mutableMapOf()

  private var cacheConfig: EndpointCacheConfig? = null
  private var resilienceConfig: ResilienceConfig? = null

  fun cache(block: CacheDslBuilder.() -> Unit) {
    val builder = CacheDslBuilder(name)
    builder.block()
    cacheConfig = builder.build()
  }

  fun resilience(block: ResilienceDslBuilder.() -> Unit) {
    val builder = ResilienceDslBuilder()
    builder.block()
    resilienceConfig = builder.build()
  }

  /**
   * Build and register the endpoint definition.
   */
  fun build(): EndpointDefinition {
    require(baseUrl.isNotBlank()) { "baseUrl is required for endpoint '$name'" }
    require(path.isNotBlank()) { "path is required for endpoint '$name'" }

    val definition = EndpointDefinition(
      name = name,
      baseUrl = baseUrl,
      path = path,
      method = method,
      cacheConfig = cacheConfig,
      resilienceConfig = resilienceConfig,
      responseType = responseType,
      headers = headers.toMap(),
    )

    registry.register(definition)
    return definition
  }
}

/**
 * DSL builder for cache configuration.
 */
class CacheDslBuilder(private val endpointName: String) {
  var enabled: Boolean = true
  var cacheName: String = "${endpointName}Cache"
  var ttl: Duration = Duration.ofMinutes(5)
  var maxSize: Long? = null
  var keyStrategy: CacheKeyStrategy = defaultCacheKeyStrategy(endpointName)

  fun build(): EndpointCacheConfig = EndpointCacheConfig(
    enabled = enabled,
    cacheName = cacheName,
    ttl = ttl,
    maxSize = maxSize,
    keyStrategy = keyStrategy,
  )
}

/**
 * DSL builder for resilience configuration.
 */
class ResilienceDslBuilder {
  var timeout: Duration? = null
  private var circuitBreakerConfig: CircuitBreakerConfig? = null
  private var retryConfig: RetryConfig? = null

  fun circuitBreaker(block: CircuitBreakerDslBuilder.() -> Unit) {
    val builder = CircuitBreakerDslBuilder()
    builder.block()
    circuitBreakerConfig = builder.build()
  }

  fun retry(block: RetryDslBuilder.() -> Unit) {
    val builder = RetryDslBuilder()
    builder.block()
    retryConfig = builder.build()
  }

  fun build(): ResilienceConfig = ResilienceConfig(
    timeout = timeout,
    circuitBreaker = circuitBreakerConfig,
    retry = retryConfig,
  )
}

/**
 * DSL builder for circuit breaker configuration.
 */
class CircuitBreakerDslBuilder {
  var failureRateThreshold: Float = 50.0f
  var waitDurationInOpenState: Duration = Duration.ofSeconds(10)
  var slidingWindowSize: Int = 10
  var minimumNumberOfCalls: Int = 5
  var permittedNumberOfCallsInHalfOpenState: Int = 3

  fun build(): CircuitBreakerConfig = CircuitBreakerConfig(
    failureRateThreshold = failureRateThreshold,
    waitDurationInOpenState = waitDurationInOpenState,
    slidingWindowSize = slidingWindowSize,
    minimumNumberOfCalls = minimumNumberOfCalls,
    permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState,
  )
}

/**
 * DSL builder for retry configuration.
 */
class RetryDslBuilder {
  var maxAttempts: Int = 3
  var waitDuration: Duration = Duration.ofMillis(500)
  var retryOnResult: ((Any?) -> Boolean)? = null

  fun build(): RetryConfig = RetryConfig(
    maxAttempts = maxAttempts,
    waitDuration = waitDuration,
    retryOnResult = retryOnResult,
  )
}

/**
 * DSL function to define an endpoint.
 */
fun EndpointRegistry.endpoint(name: String, block: EndpointDslBuilder.() -> Unit): EndpointDefinition {
  val builder = EndpointDslBuilder(name, this)
  builder.block()
  return builder.build()
}
