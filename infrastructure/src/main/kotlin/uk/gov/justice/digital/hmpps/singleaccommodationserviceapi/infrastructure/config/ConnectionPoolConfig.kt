package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.HttpResources
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class ConnectionPoolConfig(
  private val connectionPoolProperties: ConnectionPoolProperties,
) {

  @Bean
  fun defaultHttpConnectionProvider(): ConnectionProvider {
    val provider = ConnectionProvider.builder(connectionPoolProperties.poolName)
      .maxConnections(connectionPoolProperties.maxConnections)
      .maxIdleTime(connectionPoolProperties.maxIdleTime)
      .maxLifeTime(connectionPoolProperties.maxLifeTime)
      .evictInBackground(connectionPoolProperties.evictionInterval)
      .build()

    HttpResources.set(provider)
    return provider
  }
}

@ConfigurationProperties(prefix = "service.connection-pool")
data class ConnectionPoolProperties(
  val maxConnections: Int = DEFAULT_MAX_CONNECTIONS,
  val maxIdleTime: Duration = DEFAULT_MAX_IDLE_TIME,
  val maxLifeTime: Duration = DEFAULT_MAX_LIFE_TIME,
  val evictionInterval: Duration = DEFAULT_EVICTION_INTERVAL,
  val poolName: String = DEFAULT_POOL_NAME,
) {
  companion object {
    private const val DEFAULT_MAX_IDLE_TIME_SECONDS = 30L
    private const val DEFAULT_MAX_LIFE_TIME_MINUTES = 5L
    private const val DEFAULT_EVICTION_INTERVAL_SECONDS = 30L

    // Matches Reactor Netty's own default pool; a custom builder otherwise falls back to max(cores, 8) * 2
    const val DEFAULT_MAX_CONNECTIONS = 500
    const val DEFAULT_POOL_NAME = "default-http-pool"
    val DEFAULT_MAX_IDLE_TIME: Duration = Duration.ofSeconds(DEFAULT_MAX_IDLE_TIME_SECONDS)
    val DEFAULT_MAX_LIFE_TIME: Duration = Duration.ofMinutes(DEFAULT_MAX_LIFE_TIME_MINUTES)
    val DEFAULT_EVICTION_INTERVAL: Duration = Duration.ofSeconds(DEFAULT_EVICTION_INTERVAL_SECONDS)
  }
}
