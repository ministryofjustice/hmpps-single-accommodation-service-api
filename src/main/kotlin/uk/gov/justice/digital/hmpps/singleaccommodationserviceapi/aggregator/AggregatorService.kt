package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatedResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorRegistry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointError
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointExecutor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointRegistry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointResult

@Service
class AggregatorService(
  private val endpointRegistry: EndpointRegistry,
  private val aggregatorRegistry: AggregatorRegistry,
  private val endpointExecutor: EndpointExecutor,
) {

  fun aggregate(
    endpointNames: Set<String>,
    params: Map<String, Any?> = emptyMap(),
  ): Mono<AggregatedResponse> {
    val endpoints = endpointNames.mapNotNull { name ->
      endpointRegistry.get(name)?.let { name to it }
    }

    if (endpoints.isEmpty()) {
      return Mono.just(
        AggregatedResponse(
          results = emptyMap(),
        ),
      )
    }

    val resultMonos = endpoints.map { (name, endpoint) ->
      endpointExecutor.execute<Any>(endpoint, params)
        .map { result -> name to result }
    }

    return Flux.fromIterable(resultMonos)
      .flatMap { it }
      .collectMap({ it.first }, { it.second })
      .map { results ->
        AggregatedResponse(
          results = results,
        )
      }
      .onErrorResume { error ->
        // This should never happen, but ensure we never fail
        Mono.just(
          AggregatedResponse(
            results = endpoints.associate { (name, _) ->
              name to EndpointResult.Failure(
                EndpointError(
                  endpointName = name,
                  message = "Unexpected error: ${error.message}",
                  errorType = EndpointError.ErrorType.UNKNOWN_ERROR,
                ),
              )
            },
          ),
        )
      }
  }

  fun aggregateByName(
    aggregatorName: String,
    params: Map<String, Any?> = emptyMap(),
  ): Mono<AggregatedResponse> {
    val endpointNames = aggregatorRegistry.getRequired(aggregatorName)
    return aggregate(endpointNames, params)
  }

  fun <T> executeSingle(
    endpointName: String,
    params: Map<String, Any?> = emptyMap(),
  ): Mono<EndpointResult<T>> {
    val endpoint = endpointRegistry.getRequired(endpointName)
    return endpointExecutor.execute(endpoint, params)
  }
}
