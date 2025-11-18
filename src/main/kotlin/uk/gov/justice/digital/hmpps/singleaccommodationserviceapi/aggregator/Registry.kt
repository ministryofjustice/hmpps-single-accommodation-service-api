package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AggregatorRegistry {
  private val aggregators = ConcurrentHashMap<String, Set<String>>()

  fun register(aggregatorName: String, endpointNames: Set<String>) {
    if (aggregators.containsKey(aggregatorName)) {
      throw IllegalArgumentException("Aggregator '$aggregatorName' is already registered")
    }
    aggregators[aggregatorName] = endpointNames
  }

  fun get(aggregatorName: String): Set<String>? = aggregators[aggregatorName]

  fun getRequired(aggregatorName: String): Set<String> = aggregators[aggregatorName]
    ?: throw IllegalArgumentException("Aggregator '$aggregatorName' is not registered")

  fun contains(aggregatorName: String): Boolean = aggregators.containsKey(aggregatorName)
}

@Component
class EndpointRegistry {
  private val endpoints = ConcurrentHashMap<String, EndpointDefinition>()

  fun register(endpoint: EndpointDefinition) {
    if (endpoints.containsKey(endpoint.name)) {
      throw IllegalArgumentException("Endpoint '${endpoint.name}' is already registered")
    }
    endpoints[endpoint.name] = endpoint
  }

  fun get(name: String): EndpointDefinition? = endpoints[name]

  fun getRequired(name: String): EndpointDefinition = endpoints[name] ?: throw IllegalArgumentException("Endpoint '$name' is not registered")

  fun contains(name: String): Boolean = endpoints.containsKey(name)
}
