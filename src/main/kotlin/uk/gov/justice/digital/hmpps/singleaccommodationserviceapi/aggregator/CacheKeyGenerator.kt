package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.stereotype.Component
import org.springframework.web.util.UriTemplate
import java.net.URI

/**
 * Generates cache keys from endpoint definitions and parameters.
 * Handles path variables, query parameters, and headers.
 */
@Component
class CacheKeyGenerator {

  /**
   * Generate a cache key for an endpoint with given parameters.
   * Parameters can include path variables, query parameters, and headers.
   */
  fun generateKey(
    endpoint: EndpointDefinition,
    params: Map<String, Any?>,
  ): String {
    val cacheConfig = endpoint.cacheConfig
      ?: throw IllegalStateException("Endpoint '${endpoint.name}' does not have cache configuration")

    // Extract path variables from the path template
    val pathVariables = extractPathVariables(endpoint.path, params)

    // Combine all parameters for key generation
    val allParams = buildMap {
      putAll(pathVariables)
      // Add query params if present
      params.filterKeys { !it.startsWith("path:") && !it.startsWith("header:") }
        .forEach { (key, value) -> put(key, value) }
    }

    return cacheConfig.keyStrategy(allParams)
  }

  /**
   * Extract path variables from a URI template and parameters.
   */
  private fun extractPathVariables(pathTemplate: String, params: Map<String, Any?>): Map<String, Any?> {
    val template = UriTemplate(pathTemplate)
    val pathVariables = mutableMapOf<String, Any?>()

    // Extract variable names from template
    val variableNames = template.variableNames

    variableNames.forEach { varName ->
      // Check for path: prefix first, then direct match
      val value = params["path:$varName"] ?: params[varName]
      if (value != null) {
        pathVariables[varName] = value
      }
    }

    return pathVariables
  }

  /**
   * Build the full URI from endpoint definition and parameters.
   */
  fun buildUri(endpoint: EndpointDefinition, params: Map<String, Any?>): URI {
    val template = UriTemplate(endpoint.path)
    val pathVariables = extractPathVariables(endpoint.path, params)

    val expandedPath = template.expand(pathVariables)
    val uriBuilder = URI.create(endpoint.baseUrl).resolve(expandedPath).toASCIIString()

    // Add query parameters if present
    val queryParams = params
      .filterKeys { !it.startsWith("path:") && !it.startsWith("header:") }
      .filter { it.value != null }
      .map { "${it.key}=${it.value}" }
      .joinToString("&")

    return if (queryParams.isNotEmpty()) {
      URI.create("$uriBuilder?$queryParams")
    } else {
      URI.create(uriBuilder)
    }
  }
}
