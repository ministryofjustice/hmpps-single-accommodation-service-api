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
class RestClientForAuthorizationCodeFlowConfig(
  private val restClientBuilder: RestClient.Builder,
  private val httpAuthService: HttpAuthService,
) {

  @Bean
  fun nomisUserRolesClient(@Value($$"${service.nomis-user-roles.base-url}") baseUrl: String) = createClient(
    baseUrl,
    type = NomisUserRolesClient::class,
  )

  private fun <T : Any> createClient(baseUrl: String, type: KClass<T>): T {
    val client = restClientBuilder
      .requestFactory(withTimeouts(Duration.ofSeconds(1), Duration.ofSeconds(5)))
      .baseUrl(baseUrl)
      .requestInterceptor { request, body, execution ->
        val jwt = httpAuthService.getJwt()
        request.headers.setBearerAuth(jwt.tokenValue)
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
