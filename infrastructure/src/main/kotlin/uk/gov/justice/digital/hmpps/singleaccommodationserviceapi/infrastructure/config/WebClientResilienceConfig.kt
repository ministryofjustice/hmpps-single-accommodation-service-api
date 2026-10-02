package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.HttpResources
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import kotlin.math.max

@Configuration
class WebClientResilienceConfig {
  private val log = LoggerFactory.getLogger(javaClass)

  @Bean(destroyMethod = "dispose")
  fun hmppsConnectionProvider(
    @Value($$"${service.http-client.max-idle-time:30s}") maxIdleTime: Duration,
    @Value($$"${service.http-client.background-eviction:15s}") backgroundEviction: Duration,
  ): ConnectionProvider {
    val provider = hmppsHttpConnectionProvider(maxIdleTime, backgroundEviction)
    HttpResources.set(provider)
    log.info(
      "HTTP connection pool installed: maxIdleTime={}, backgroundEviction={}, maxConnections={}",
      maxIdleTime,
      backgroundEviction,
      provider.maxConnections(),
    )
    return provider
  }
}

private const val HTTP_POOL_NAME = "hmpps-http"

/** Same as [reactor.netty.http.HttpResources]  */
private const val DEFAULT_HTTP_POOL_MAX_CONNECTIONS = 500

internal fun hmppsHttpConnectionProvider(
  maxIdleTime: Duration,
  backgroundEviction: Duration,
): ConnectionProvider {
  val maxConnections = max(ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS, DEFAULT_HTTP_POOL_MAX_CONNECTIONS)
  return ConnectionProvider.builder(HTTP_POOL_NAME)
    .maxConnections(maxConnections)
    .maxIdleTime(maxIdleTime)
    .evictInBackground(backgroundEviction)
    .lifo()
    .build()
}
