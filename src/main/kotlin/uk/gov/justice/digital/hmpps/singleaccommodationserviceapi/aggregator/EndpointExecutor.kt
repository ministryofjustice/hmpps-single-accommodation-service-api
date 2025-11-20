package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.EndpointCacheManager

@Component
class EndpointExecutor(
  private val webClientBuilder: WebClient.Builder,
  private val cacheManager: EndpointCacheManager,
  private val resilienceConfigurer: ResilienceConfigurer,
  private val cacheKeyGenerator: CacheKeyGenerator,
  private val objectMapper: ObjectMapper,
) {

  @Suppress("UNCHECKED_CAST")
  fun <T> execute(
    endpoint: EndpointDefinition,
    params: Map<String, Any?>,
  ): Mono<CallResult<T>> {
    val startTime = System.currentTimeMillis()
    val timestamp = startTime

    val cacheConfig = endpoint.cacheConfig


    val cacheKey = cacheConfig?.let { cacheKeyGenerator.generateKey(endpoint, params) }

    // Check cache first if caching is enabled
    if (cacheConfig != null && cacheConfig.enabled && cacheKey != null) {
      val cache = cacheManager.getCache(cacheConfig)
      val cachedValue = cache.get(cacheKey)?.get()
      if (cachedValue != null) {
        val duration = System.currentTimeMillis() - startTime
        val meta =
          CallMeta(
            endpoint = endpoint.name,
            fromCache = true,
            duration = duration,
            timestamp = timestamp,
            cacheId = cacheKey
          )
        @Suppress("UNCHECKED_CAST") val typedValue: T = cachedValue as T
        return Mono.just(CallResult.Success(typedValue, meta))
      }
    }

    // Get circuit breaker state before execution
    val circuitBreakerState = resilienceConfigurer.getCircuitBreakerState(endpoint)

    // Build the request
    val uri = cacheKeyGenerator.buildUri(endpoint, params)
    val webClient = webClientBuilder.baseUrl(endpoint.baseUrl).build()

    // Build headers map
    val headersMap = buildMap<String, String> {
      putAll(endpoint.headers)
      params.filterKeys { it.startsWith("header:") }
        .forEach { (key, value) ->
          put(key.removePrefix("header:"), value?.toString() ?: "")
        }
    }

    val responseMono: Mono<T> =
      webClient
        .method(endpoint.method)
        .uri(uri)
        .apply { headersMap.forEach { (key, value) -> header(key, value) } }
        .retrieve()
        .bodyToMono(String::class.java)
        .flatMap { body ->
          try {
            @Suppress("UNCHECKED_CAST")
            val result: T =
              if (endpoint.responseType == String::class) {
                body as T
              } else {
                objectMapper.readValue(body, endpoint.responseType.java) as T
              }
            Mono.just(result)
          } catch (e: Exception) {
            Mono.error(e)
          }
        }



    val resilientMono = resilienceConfigurer.applyResilience(endpoint, responseMono)

    @Suppress("UNCHECKED_CAST")
    val resultMono: Mono<CallResult<T>> =
      resilientMono
        .doOnNext { result ->
          // Cache the result if caching is enabled
          if (cacheConfig != null && cacheConfig.enabled && cacheKey != null) {
            val cache = cacheManager.getCache(cacheConfig)
            cache.put(cacheKey, result)
          }
        }
        .map { result: T ->
          val duration = System.currentTimeMillis() - startTime
          val meta =
            createCallMeta(
              endpoint,
              duration,
              timestamp,
              circuitBreakerState,
              cacheKey
            )
          createSuccessResult(result, meta)
        }
        .onErrorResume { error ->
          val duration = System.currentTimeMillis() - startTime
          val meta =
            createCallMeta(
              endpoint,
              duration,
              timestamp,
              circuitBreakerState,
              cacheKey
            )
          val errorInfo = createErrorInfo(endpoint.name, error)
          Mono.just(createErrorResult(errorInfo, meta))
        } as
        Mono<CallResult<T>>

    return resultMono
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> createSuccessResult(data: T, meta: CallMeta): CallResult<T> {
    return CallResult.Success(data, meta) as CallResult<T>
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T> createErrorResult(errorInfo: ErrorInfo, meta: CallMeta): CallResult<T> {
    return CallResult.Error(errorInfo, meta) as CallResult<T>
  }

  private fun createCallMeta(
    endpoint: EndpointDefinition,
    duration: Long,
    timestamp: Long,
    circuitBreakerState: CircuitBreakerState?,
    cacheKey: String?
  ): CallMeta {
    val retryMetrics = resilienceConfigurer.getRetryMetrics(endpoint)

    return CallMeta(
      endpoint = endpoint.name,
      fromCache = false,
      duration = duration,
      timestamp = timestamp,
      retries = retryMetrics?.first,
      circuitBreakerState = circuitBreakerState,
      attempt = retryMetrics?.second,
      cacheId = cacheKey
    )
  }

  private fun createErrorInfo(endpointName: String, error: Throwable): ErrorInfo {
    val errorType =
      when {
        error is java.util.concurrent.TimeoutException -> ErrorType.TIMEOUT
        error is org.springframework.web.reactive.function.client.WebClientException ->
          ErrorType.NETWORK

        error is
          org.springframework.web.reactive.function.client.WebClientResponseException -> {
          val httpError =
            error as
              org.springframework.web.reactive.function.client.WebClientResponseException
          when (httpError.statusCode.value()) {
            401, 403 -> ErrorType.AUTH
            else -> ErrorType.NETWORK
          }
        }

        error.message?.contains("CircuitBreaker", ignoreCase = true) == true ->
          ErrorType.CIRCUIT_BREAKER

        error.message?.contains("Retry", ignoreCase = true) == true ->
          ErrorType.RETRY_EXHAUSTED

        else -> ErrorType.UNKNOWN
      }

    return ErrorInfo(
      type = errorType,
      message = error.message ?: "Unknown error",
      endpoint = endpointName,
      originalError = error
    )
  }
}
