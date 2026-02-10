@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.toEntity
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class HealthPingClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
) {

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services.
  @Bean
  fun hmppsAuthHealthRestClient(builder: RestClient.Builder): RestClient = builder.healthRestClient(hmppsAuthBaseUri, Duration.ofSeconds(2))

  fun RestClient.Builder.healthRestClient(
    url: String,
    healthTimeout: Duration = Duration.ofSeconds(2),
  ): RestClient {
    val httpClient = HttpClient.newBuilder()
      .connectTimeout(healthTimeout)
      .build()

    val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
      setReadTimeout(healthTimeout)
    }

    return this
      .baseUrl(url)
      .requestFactory(requestFactory)
      .build()
  }
}

@Component("hmppsAuth")
class HmppsAuthHealthPing(hmppsAuthHealthRestClient: RestClient) : HealthPingCheck(hmppsAuthHealthRestClient)

abstract class HealthPingCheck(private val restClient: RestClient) : HealthIndicator {
  override fun health(): Health = restClient.ping()
}

private fun RestClient.ping(): Health = try {
  val response = get()
    .uri("/health/ping")
    .retrieve()
    .toEntity<String>()

  Health.up()
    .withDetail("HttpStatus", response.statusCode)
    .build()
} catch (ex: HttpStatusCodeException) {
  Health.down(ex)
    .withDetail("HttpStatus", ex.statusCode)
    .withDetail("body", ex.responseBodyAsString)
    .build()
} catch (ex: RestClientException) {
  Health.down(ex).build()
}
