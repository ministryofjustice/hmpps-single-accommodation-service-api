package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatedResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorRegistry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.CallMeta
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointExecutor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.EndpointRegistry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.CallResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.ErrorInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.ErrorType

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
          data = emptyMap(),
          meta = emptyMap(),
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
        val data = mutableMapOf<String, Any>()
        val meta = mutableMapOf<String, CallMeta>()
        val errors = mutableMapOf<String, ErrorInfo>()

        results.forEach { (endpointName, callResult) ->
          when (callResult) {
            is CallResult.Success -> {
              data[endpointName] = callResult.data
              meta[endpointName] = callResult.meta
            }
            is CallResult.Error -> {
              meta[endpointName] = callResult.meta
              errors[endpointName] = callResult.error
            }
          }
        }

        AggregatedResponse(
          data = data,
          meta = meta,
          errors = errors.takeIf { it.isNotEmpty() },
        )
      }
      .onErrorResume { error ->
        // This should never happen, but ensure we never fail
        val errorMap =
          endpoints.associate { (name, _) ->
            name to
              ErrorInfo(
                type = ErrorType.UNKNOWN,
                message = "Unexpected error: ${error.message}",
                endpoint = name,
                originalError = error,
              )
          }
        Mono.just(
          AggregatedResponse(
            data = emptyMap(),
            meta = emptyMap(),
            errors = errorMap,
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
  ): Mono<CallResult<T>> {
    val endpoint = endpointRegistry.getRequired(endpointName)
    return endpointExecutor.execute(endpoint, params)
  }
}
