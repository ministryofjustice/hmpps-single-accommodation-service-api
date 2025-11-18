package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import io.github.resilience4j.reactor.retry.RetryOperator
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Configures and manages resilience patterns (circuit breakers, retries) for endpoints.
 */
@Component
class ResilienceConfigurer {
  private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()
  private val retries = ConcurrentHashMap<String, Retry>()

  private val circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults()
  private val retryRegistry = RetryRegistry.ofDefaults()

  /**
   * Get or create a circuit breaker for an endpoint.
   */
  fun getCircuitBreaker(endpoint: EndpointDefinition): CircuitBreaker? {
    val config = endpoint.resilienceConfig?.circuitBreaker ?: return null

    return circuitBreakers.computeIfAbsent(endpoint.name) {
      val breakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(config.failureRateThreshold)
        .waitDurationInOpenState(config.waitDurationInOpenState)
        .slidingWindowSize(config.slidingWindowSize)
        .minimumNumberOfCalls(config.minimumNumberOfCalls)
        .permittedNumberOfCallsInHalfOpenState(config.permittedNumberOfCallsInHalfOpenState)
        .build()

      circuitBreakerRegistry.circuitBreaker(endpoint.name, breakerConfig)
    }
  }

  /**
   * Get or create a retry policy for an endpoint.
   */
  fun getRetry(endpoint: EndpointDefinition): Retry? {
    val config = endpoint.resilienceConfig?.retry ?: return null

    return retries.computeIfAbsent(endpoint.name) {
      val retryConfigBuilder = RetryConfig.custom<Any>()
        .maxAttempts(config.maxAttempts)
        .waitDuration(config.waitDuration)

      config.retryOnResult?.let { predicate ->
        @Suppress("UNCHECKED_CAST")
        retryConfigBuilder.retryOnResult(predicate as (Any?) -> Boolean)
      }

      retryRegistry.retry(endpoint.name, retryConfigBuilder.build())
    }
  }

  /**
   * Apply resilience patterns to a Mono.
   */
  fun <T> applyResilience(
    endpoint: EndpointDefinition,
    mono: Mono<T>,
  ): Mono<T> {
    var result = mono

    // Apply timeout if configured
    endpoint.resilienceConfig?.timeout?.let { timeout ->
      result = result.timeout(timeout)
    }

    // Apply retry if configured
    getRetry(endpoint)?.let { retry ->
      result = result.transformDeferred(RetryOperator.of(retry))
    }

    // Apply circuit breaker if configured
    getCircuitBreaker(endpoint)?.let { circuitBreaker ->
      result = result.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
    }

    return result
  }
}
