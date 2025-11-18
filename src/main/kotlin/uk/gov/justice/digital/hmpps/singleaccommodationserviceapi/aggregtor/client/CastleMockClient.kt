package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregtor.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class CastleMockClient(
  builder: WebClient.Builder,
  @Value("\${castlemock-api.url}") castleMockApiBaseUrl: String,
) {
  private val webClient: WebClient =
    builder.baseUrl(castleMockApiBaseUrl).build()

  fun callEndpoint(path: String): Mono<String> = webClient.get()
    .uri(path)
    .retrieve()
    .bodyToMono(String::class.java)
    .timeout(Duration.ofMillis(500))
    .onErrorResume { Mono.empty() }
}
