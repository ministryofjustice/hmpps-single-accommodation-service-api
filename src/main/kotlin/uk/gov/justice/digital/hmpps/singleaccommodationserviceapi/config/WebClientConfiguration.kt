package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

data class WebClientConfig(
  val webClient: WebClient,
  val maxRetryAttempts: Long = 1,
  val retryOnReadTimeout: Boolean = false,
)

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps.auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
) {

  @Bean(name = ["approvedPremisesApiWebClient"])
  fun approvedPremisesApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    @Value("\${services.approved-premises-api.base-url}") approvedPremisesApiBaseUrl: String,
    @Value("\${services.approved-premises-api.timeout-ms}") approvedPremisesApiServiceUpstreamTimeoutMs: Long,
  ): WebClientConfig {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients)

    oauth2Client.setDefaultClientRegistrationId("approved-premises-api")

    return WebClientConfig(
      WebClient.builder()
        .baseUrl(approvedPremisesApiBaseUrl)
        .clientConnector(
          ReactorClientHttpConnector(
            HttpClient
              .create()
              .responseTimeout(Duration.ofMillis(approvedPremisesApiServiceUpstreamTimeoutMs))
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Duration.ofMillis(approvedPremisesApiServiceUpstreamTimeoutMs).toMillis().toInt()),
          ),
        )
        .filter(oauth2Client)
        .build(),
      retryOnReadTimeout = true,
    )
  }

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)
}
