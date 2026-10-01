package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationDataDomainClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServicesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class HttpServiceProxiesConfig(
  private val clientManager: OAuth2AuthorizedClientManager,
  @Value($$"${service.connection-timeout:1s}") private val connectionTimeout: Duration,
) {

  @Bean
  fun probationIntegrationSasDeliusClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.sas-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.sas-and-delius.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    SasAndDeliusClient::class,
    readTimeout,
  )

  @Bean
  fun probationIntegrationDeliusClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.approved-premises-and-delius.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-and-delius.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    ApprovedPremisesAndDeliusClient::class,
    readTimeout,
  )

  @Bean
  fun approvedPremisesClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.approved-premises-api.base-url}") baseUrl: String,
    @Value($$"${service.approved-premises-api.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    ApprovedPremisesClient::class,
    readTimeout,
  )

  @Bean
  fun corePersonRecordClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.core-person-record.base-url}") baseUrl: String,
    @Value($$"${service.core-person-record.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    CorePersonRecordClient::class,
    readTimeout,
  )

  @Bean
  fun prisonerSearchClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.prisoner-search.base-url}") baseUrl: String,
    @Value($$"${service.prisoner-search.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    PrisonerSearchClient::class,
    readTimeout,
  )

  @Bean
  fun commissionedRehabilitativeServicesClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.commissioned-rehabilitative-services-api.base-url}") baseUrl: String,
    @Value($$"${service.commissioned-rehabilitative-services-api.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    CommissionedRehabilitativeServicesClient::class,
    readTimeout,
  )

  @Bean
  fun tierClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.tier.base-url}") baseUrl: String,
    @Value($$"${service.tier.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    TierClient::class,
    readTimeout,
  )

  @Bean
  fun accommodationDataDomainClient(
    webClientBuilder: WebClient.Builder,
    @Value($$"${service.accommodation-data-domain.base-url}") baseUrl: String,
    @Value($$"${service.accommodation-data-domain.read-timeout}") readTimeout: Duration,
  ) = createClient(
    webClientBuilder,
    baseUrl,
    AccommodationDataDomainClient::class,
    readTimeout,
  )

  private fun <T : Any> createClient(
    webClientBuilder: WebClient.Builder,
    baseUrl: String,
    type: KClass<T>,
    readTimeout: Duration,
  ): T {
    val proxyFactory = HttpServiceProxyFactory
      .builderFor(
        WebClientAdapter.create(
          webClientBuilder.authorisedWebClient(
            authorizedClientManager = clientManager,
            registrationId = "default",
            url = baseUrl,
            timeout = readTimeout,
            connectionTimeout = connectionTimeout,
          ),
        ),
      )
      .build()

    return proxyFactory.createClient(type.java)
  }
}
