package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysClient
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class RestClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,

) {
  @Bean
  fun probationIntegrationDeliusClient(@Value($$"${service.probation-integration-delius.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ProbationIntegrationDeliusClient::class,
  )

  @Bean
  fun probationIntegrationOasysClient(@Value($$"${service.probation-integration-oasys.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ProbationIntegrationOasysClient::class,
  )

  @Bean
  fun approvedPremisesClient(@Value($$"${service.approved-premises-api.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ApprovedPremisesClient::class,
  )

  @Bean
  fun corePersonRecordClient(@Value($$"${service.core-person-record.base-url}") baseUrl: String) = createClient(
    baseUrl,
    CorePersonRecordClient::class,
  )

  private fun <T : Any> createClient(baseUrl: String, type: KClass<T>): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
      .requestInterceptor(HmppsAuthInterceptor(clientManager, "default"))
      .baseUrl(baseUrl)
      .build()

    val proxyFactory = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(client))
      .build()

    return proxyFactory.createClient(type.java)
  }

  private fun withTimeouts(connection: Duration, read: Duration) = JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connection).build())
    .also { it.setReadTimeout(read) }
}
