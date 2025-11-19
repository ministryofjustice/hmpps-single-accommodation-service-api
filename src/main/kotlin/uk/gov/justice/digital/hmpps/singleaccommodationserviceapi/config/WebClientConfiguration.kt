package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
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
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
  @param:Value("\${services.default.timeout-ms}") private val defaultUpstreamTimeoutMs: Long,
  @param:Value("\${services.default.max-response-in-memory-size-bytes}") private val defaultMaxResponseInMemorySizeBytes: Int,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )

    val provider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    manager.setAuthorizedClientProvider(provider)
    return manager
  }

  @Bean(name = ["apDeliusContextApiWebClient"])
  fun apDeliusContextApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.ap-delius-context-api.base-url}") apDeliusContextApiBaseUrl: String,
  ): WebClientConfig {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("delius-backed-apis")

    return WebClientConfig(
      WebClient.builder()
        .baseUrl(apDeliusContextApiBaseUrl)
        .filter(oauth2Client)
        .clientConnector(
          ReactorClientHttpConnector(
            HttpClient
              .create()
              .responseTimeout(Duration.ofMillis(defaultUpstreamTimeoutMs))
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Duration.ofMillis(defaultUpstreamTimeoutMs).toMillis().toInt()),
          ),
        )
        .exchangeStrategies(
          ExchangeStrategies.builder().codecs {
            it.defaultCodecs().maxInMemorySize(defaultMaxResponseInMemorySizeBytes)
          }.build(),
        )
        .build(),
      retryOnReadTimeout = true,
    )
  }
}
