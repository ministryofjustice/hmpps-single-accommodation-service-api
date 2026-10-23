@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.health

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class HealthPingClientConfig(
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
) {

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services.
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, Duration.ofSeconds(2))

  fun WebClient.Builder.healthWebClient(
    url: String,
    healthTimeout: Duration = Duration.ofSeconds(2),
  ): WebClient {
    val httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, healthTimeout.toMillis().toInt())
      .responseTimeout(healthTimeout)

    return this
      .baseUrl(url)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .build()
  }
}

@Component("hmppsAuth")
class HmppsAuthHealthPing(hmppsAuthHealthWebClient: WebClient) : HealthPingCheck(hmppsAuthHealthWebClient)

abstract class HealthPingCheck(private val webClient: WebClient) : HealthIndicator {
  override fun health(): Health = webClient.ping()
}

private fun WebClient.ping(): Health = try {
  val response = get()
    .uri("/health/ping")
    .retrieve()
    .toEntity(String::class.java)
    .block()

  Health.up()
    .withDetail("HttpStatus", response?.statusCode ?: "UNKNOWN")
    .build()
} catch (ex: WebClientResponseException) {
  Health.down(ex)
    .withDetail("HttpStatus", ex.statusCode)
    .withDetail("body", ex.responseBodyAsString)
    .build()
} catch (ex: WebClientRequestException) {
  Health.down(ex).build()
}
