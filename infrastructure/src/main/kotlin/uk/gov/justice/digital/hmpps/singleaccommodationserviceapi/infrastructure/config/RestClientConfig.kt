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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class RestClientConfig(
  private val restClientBuilder: RestClient.Builder,
  private val clientManager: OAuth2AuthorizedClientManager,

) {

  private val readTimeoutMillis = 3500L
  private val connectionTimeoutMillis = 1000L

  @Bean
  fun probationIntegrationSasDeliusClient(
    @Value($$"${service.sas-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.sas-and-delius.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    SasAndDeliusClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun probationIntegrationDeliusClient(
    @Value($$"${service.approved-premises-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-and-delius.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    ApprovedPremisesAndDeliusClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun probationIntegrationOasysClient(
    @Value($$"${service.approved-premises-and-oasys.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-and-oasys.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    ApprovedPremisesAndOasysClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun approvedPremisesClient(
    @Value($$"${service.approved-premises-api.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-api.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    ApprovedPremisesClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun corePersonRecordClient(
    @Value($$"${service.core-person-record.base-url}") baseUrl: String,
    @Value($$"${service.core-person-record.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    CorePersonRecordClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun commissionedRehabilitativeServicesClient(
    @Value($$"${service.commissioned-rehabilitative-services-api.base-url}") baseUrl: String,
    @Value($$"${service.commissioned-rehabilitative-services-api.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    CommissionedRehabilitativeServicesClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun tierClient(
    @Value($$"${service.tier.base-url}") baseUrl: String,
    @Value($$"${service.tier.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    TierClient::class,
    Duration.ofMillis(readTimeout),
  )

  @Bean
  fun accommodationDataDomainClient(
    @Value($$"${service.accommodation-data-domain.base-url}") baseUrl: String,
    @Value($$"${service.accommodation-data-domain.read-timeout}") readTimeout: Long,
  ) = createClient(
    baseUrl,
    AccommodationDataDomainClient::class,
    Duration.ofMillis(readTimeout),
  )

  private fun <T : Any> createClient(
    baseUrl: String,
    type: KClass<T>,
    readTimeout: Duration = Duration.ofMillis(readTimeoutMillis),
  ): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(Duration.ofSeconds(connectionTimeoutMillis), readTimeout))
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
