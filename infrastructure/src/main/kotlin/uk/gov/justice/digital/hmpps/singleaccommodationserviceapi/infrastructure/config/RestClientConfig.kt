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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.ApprovedPremisesAndOasysClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServicesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class RestClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,
  @Value($$"${service.connection-timeout:1s}") private val connectionTimeout: Duration,
) {

  @Bean
  fun probationIntegrationSasDeliusClient(
    @Value($$"${service.sas-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.sas-and-delius.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    SasAndDeliusClient::class,
    readTimeout,
  )

  @Bean
  fun probationIntegrationDeliusClient(
    @Value($$"${service.approved-premises-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-and-delius.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    ApprovedPremisesAndDeliusClient::class,
    readTimeout,
  )

  @Bean
  fun probationIntegrationOasysClient(
    @Value($$"${service.approved-premises-and-oasys.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-and-oasys.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    ApprovedPremisesAndOasysClient::class,
    readTimeout,
  )

  @Bean
  fun approvedPremisesClient(
    @Value($$"${service.approved-premises-api.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-api.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    ApprovedPremisesClient::class,
    readTimeout,
  )

  @Bean
  fun corePersonRecordClient(
    @Value($$"${service.core-person-record.base-url}") baseUrl: String,
    @Value($$"${service.core-person-record.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    CorePersonRecordClient::class,
    readTimeout,
  )

  @Bean
  fun prisonerSearchClient(
    @Value($$"${service.prisoner-search.base-url}") baseUrl: String,
    @Value($$"${service.prisoner-search.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    PrisonerSearchClient::class,
    readTimeout,
  )

  @Bean
  fun commissionedRehabilitativeServicesClient(
    @Value($$"${service.commissioned-rehabilitative-services-api.base-url}") baseUrl: String,
    @Value($$"${service.commissioned-rehabilitative-services-api.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    CommissionedRehabilitativeServicesClient::class,
    readTimeout,
  )

  @Bean
  fun tierClient(
    @Value($$"${service.tier.base-url}") baseUrl: String,
    @Value($$"${service.tier.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    TierClient::class,
    readTimeout,
  )

  @Bean
  fun accommodationDataDomainClient(
    @Value($$"${service.accommodation-data-domain.base-url}") baseUrl: String,
    @Value($$"${service.accommodation-data-domain.read-timeout}") readTimeout: Duration,
  ) = createClient(
    baseUrl,
    AccommodationDataDomainClient::class,
    readTimeout,
  )

  private fun <T : Any> createClient(
    baseUrl: String,
    type: KClass<T>,
    readTimeout: Duration,
  ): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(connectionTimeout, readTimeout))
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
