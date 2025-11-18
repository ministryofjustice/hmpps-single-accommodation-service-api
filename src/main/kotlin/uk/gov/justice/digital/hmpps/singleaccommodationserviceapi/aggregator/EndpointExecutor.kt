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

  fun <T> execute(
    endpoint: EndpointDefinition,
    params: Map<String, Any?>,
  ): Mono<EndpointResult<T>> {
    // Check cache first if caching is enabled
    val cacheConfig = endpoint.cacheConfig
    if (cacheConfig != null && cacheConfig.enabled) {
      val cache = cacheManager.getCache(cacheConfig)
      val cacheKey = cacheKeyGenerator.generateKey(endpoint, params)

      val cachedValue = cache.get(cacheKey)?.get()
      if (cachedValue != null) {
        @Suppress("UNCHECKED_CAST")
        return Mono.just(EndpointResult.Success(cachedValue as T))
      }
    }

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

    // Build and execute request with resilience patterns
    val responseMono: Mono<T> = webClient
      .method(endpoint.method)
      .uri(uri)
      .apply {
        headersMap.forEach { (key, value) ->
          header(key, value)
        }
      }
      .retrieve()
      .bodyToMono(String::class.java)
      .flatMap { body ->
        try {
          @Suppress("UNCHECKED_CAST")
          val result = if (endpoint.responseType == String::class) {
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

    return resilientMono
      .map { result: T ->
        // Cache the result if caching is enabled
        if (cacheConfig != null && cacheConfig.enabled) {
          val cache = cacheManager.getCache(cacheConfig)
          val cacheKey = cacheKeyGenerator.generateKey(endpoint, params)
          cache.put(cacheKey, result)
        }
        EndpointResult.Success(result) as EndpointResult<T>
      }
      .onErrorResume { error ->
        Mono.just(
          EndpointResult.Failure(
            createEndpointError(endpoint.name, error),
          ),
        )
      }
  }

  private fun createEndpointError(endpointName: String, error: Throwable): EndpointError {
    val errorType = when {
      error is java.util.concurrent.TimeoutException -> EndpointError.ErrorType.TIMEOUT
      error is org.springframework.web.reactive.function.client.WebClientException -> EndpointError.ErrorType.NETWORK_ERROR
      error is org.springframework.web.reactive.function.client.WebClientResponseException -> EndpointError.ErrorType.HTTP_ERROR
      else -> EndpointError.ErrorType.UNKNOWN_ERROR
    }

    return EndpointError(
      endpointName = endpointName,
      message = error.message ?: "Unknown error",
      errorType = errorType,
      cause = error.javaClass.simpleName,
    )
  }
}
