package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service.AggregatorPocService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.RedisIntegrationResponse

@RestController
class AggregatorPocController(
  private val aggregatorPocService: AggregatorPocService,
) {

  @GetMapping("/redis-integration", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun redisIntegrationPOC(): ResponseEntity<RedisIntegrationResponse> {
    val response = aggregatorPocService.orchestrateCallsThatWriteToCache()
    return ResponseEntity.ok(response)
  }

  @GetMapping("/webflux-resilience", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun webfluxResiliencePOC(): Flux<String> = aggregatorPocService.webfluxResilience()
}
