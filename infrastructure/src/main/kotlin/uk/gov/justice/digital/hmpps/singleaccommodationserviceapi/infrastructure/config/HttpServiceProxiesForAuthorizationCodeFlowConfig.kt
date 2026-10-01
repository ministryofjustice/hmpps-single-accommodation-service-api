package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import java.net.http.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

@Configuration
class HttpServiceProxiesForAuthorizationCodeFlowConfig(
  private val httpAuthService: HttpAuthService,
) {

  @Bean
  fun nomisUserRolesClient(
    restClientBuilder: RestClient.Builder,
    @Value($$"${service.nomis-user-roles.base-url}") baseUrl: String,
  ) = createClient(
    restClientBuilder,
    baseUrl,
    type = NomisUserRolesClient::class,
  )

  /**
   * This uses a RestClient with a configuration that does not support the hmpps proxy config
   * (unlike the HttpServiceProxies defined in [HttpServiceProxiesConfig])
   *
   * As part of NOMIS support this code will need to be updated to either
   *
   * a) Use a RestClient builder defined in hmpps-kotlin-lib that internally supports proxy configuration
   * (i.e. following the pattern used by [HttpServiceProxiesConfig]), noting that these builders do not
   * yet exist in hmpps-kotlin-lib so will need to be added. If doing this HttpServiceProxiesConfig
   * should be updated to also use RestClient, and error handling code should also be updated (basically
   * revert the commit adding this comment)
   * b) Use a WebClient, built using the builders defined in hmpps-kotlin-lib (if doing this, also remove
   * the rest-client library and remove remaining error handling code that deals with its exceptions e.g.
   * handling ResourceAccessException)
   *
   * This code was deliberately not updated to use WebClient when migrating [HttpServiceProxiesConfig]
   * because it has bespoke behaviour around Authorization headers, and we're not in a position
   * to test NOMIS logins before the change is made to ensure there is no regression in functionality
   */
  private fun <T : Any> createClient(
    restClientBuilder: RestClient.Builder,
    baseUrl: String,
    type: KClass<T>,
  ): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
      .baseUrl(baseUrl)
      .requestInterceptor { request, body, execution ->
        if (!request.headers.containsHeader("Authorization")) {
          val jwt = httpAuthService.getJwt()
          request.headers.setBearerAuth(jwt.tokenValue)
        }
        execution.execute(request, body)
      }
      .build()

    val proxyFactory = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(client))
      .build()

    return proxyFactory.createClient(type.java)
  }

  private fun withTimeouts(connection: Duration, read: Duration) = JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(connection).build())
    .also { it.setReadTimeout(read) }
}
