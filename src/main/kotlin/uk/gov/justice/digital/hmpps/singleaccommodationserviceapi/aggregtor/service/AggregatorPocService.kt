package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.client.PhoneClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.client.CastleMockClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.RedisIntegrationResponse

@Service
class AggregatorPocService(
  private val phoneClient: PhoneClient,
  private val castleMockClient: CastleMockClient,
) {
  fun orchestrateCallsThatWriteToCache(): RedisIntegrationResponse {
    return RedisIntegrationResponse(
      primaryPhone = phoneClient.getPhoneById(id = 1L),
    )
  }

  fun webfluxResilience(): Flux<String> =
    Flux.merge(
      castleMockClient.callEndpoint("/customers"),
      castleMockClient.callEndpoint("/employees"),
      castleMockClient.callEndpoint("/products")
    )
}