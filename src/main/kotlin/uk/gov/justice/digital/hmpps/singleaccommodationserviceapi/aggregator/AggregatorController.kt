package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatedResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.CallResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service.AggregatorService

@RestController
@RequestMapping("/aggregator")
class AggregatorController(
  private val aggregatorService: AggregatorService,
) {

  @GetMapping("/endpoint/{endpointName}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun executeEndpoint(
    @PathVariable endpointName: String,
    @RequestParam(required = false) userId: String?,
    @RequestParam(required = false) postId: String?,
  ): Mono<ResponseEntity<CallResult<*>>> {
    val params = buildMap<String, Any?> {
      userId?.let { put("userId", it) }
      postId?.let { put("postId", it) }
    }

    return aggregatorService.executeSingle<Any>(endpointName, params)
      .map { result -> ResponseEntity.ok(result) }
  }

  @GetMapping("/group/{aggregatorName}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun executeAggregator(
    @PathVariable aggregatorName: String,
    @RequestParam(required = false) userId: String?,
    @RequestParam(required = false) postId: String?,
  ): Mono<ResponseEntity<AggregatedResponse>> {
    println("Executing aggregator: $aggregatorName")
    val params = buildMap<String, Any?> {
      userId?.let { put("userId", it) }
      postId?.let { put("postId", it) }
    }

    return aggregatorService.aggregateByName(aggregatorName, params)
      .map { response -> ResponseEntity.ok(response) }
  }

  @GetMapping("/parallel", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun executeParallel(
    @RequestParam endpoints: String,
    @RequestParam(required = false) userId: String?,
    @RequestParam(required = false) postId: String?,
  ): Mono<ResponseEntity<AggregatedResponse>> {
    val endpointNames = endpoints.split(",").map { it.trim() }.toSet()
    val params = buildMap<String, Any?> {
      userId?.let { put("userId", it) }
      postId?.let { put("postId", it) }
    }

    return aggregatorService.aggregate(endpointNames, params)
      .map { response -> ResponseEntity.ok(response) }
  }
}
