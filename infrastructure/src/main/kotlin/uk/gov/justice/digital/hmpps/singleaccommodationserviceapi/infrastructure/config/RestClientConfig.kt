package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.HmppsAuthInterceptor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationDataDomainClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.ProbationIntegrationOasysClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
open class RestClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,

) {
  @Bean
  open fun probationIntegrationDeliusClient(@Value($$"${service.probation-integration-delius.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ProbationIntegrationDeliusClient::class,
  )

  @Bean
  open fun probationIntegrationOasysClient(@Value($$"${service.probation-integration-oasys.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ProbationIntegrationOasysClient::class,
  )

  @Bean
  open fun approvedPremisesClient(@Value($$"${service.approved-premises-api.base-url}") baseUrl: String) = createClient(
    baseUrl,
    ApprovedPremisesClient::class,
  )

  @Bean
  open fun corePersonRecordClient(@Value($$"${service.core-person-record.base-url}") baseUrl: String) = createClient(
    baseUrl,
    CorePersonRecordClient::class,
  )

  @Bean
  open fun tierClient(@Value($$"${service.tier.base-url}") baseUrl: String) = createClient(
    baseUrl,
    TierClient::class,
  )

  @Bean
  open fun prisonerSearchClient(@Value($$"${service.prisoner-search.base-url}") baseUrl: String) = createClient(
    baseUrl,
    PrisonerSearchClient::class,
  )

  @Bean
  fun accommodationDataDomainClient(@Value($$"${service.accommodation-data-domain.base-url}") baseUrl: String) = createClient(
    baseUrl,
    AccommodationDataDomainClient::class,
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
