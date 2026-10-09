package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.HttpResources
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

/**
 * Ensures stale/idle Reactor Netty HTTP connections are actively cleaned up.
 *
 * All of our outbound `WebClient`s (see [HttpServiceProxiesConfig]) are built via
 * `hmpps-kotlin-lib`'s `WebClient.Builder.authorisedWebClient(...)` extension. Internally that
 * extension (and the OAuth2 client-credentials token-fetching client it wires up) create their
 * Reactor Netty `HttpClient` via the no-arg `HttpClient.create()` factory method - that factory
 * always uses Reactor Netty's single JVM-wide **default** connection pool
 * ([reactor.netty.http.HttpResources]), rather than a pool we control.
 *
 * That default pool does not configure `maxIdleTime`, `maxLifeTime` or background eviction, so
 * connections that have gone stale (e.g. idle connections silently closed by a load balancer or
 * NAT) can be handed back out of the pool and reused, leading to errors such as
 * "Connection reset by peer".
 *
 * [HttpResources.set] replaces that JVM-wide default pool, so configuring it here is enough to
 * apply eviction to every `WebClient`/`HttpClient` created via `HttpClient.create()` - including
 * the ones created inside `hmpps-kotlin-lib` - without having to re-implement its OAuth2/proxy
 * wiring ourselves.
 */
@Configuration
class ConnectionPoolConfig {

  companion object {
    private val MAX_IDLE_TIME: Duration = Duration.ofSeconds(30)
    private val MAX_LIFE_TIME: Duration = Duration.ofMinutes(5)
    private val EVICTION_INTERVAL: Duration = Duration.ofSeconds(30)
  }

  /**
   * Builds the pool and installs it as Reactor Netty's JVM-wide default, as soon as this
   * configuration bean is created by Spring (i.e. during context startup, before any of our
   * [HttpServiceProxiesConfig] WebClient beans are used to make requests).
   *
   * Settings:
   * - `maxIdleTime`: a pooled connection unused for longer than this is evicted rather than reused
   * - `maxLifeTime`: a connection is recycled after this long even if still in use, to prevent
   *   state drift / silently-dead long-lived connections
   * - `evictInBackground`: proactively sweeps the pool for idle/expired connections on a fixed
   *   schedule, rather than only checking when a connection is next acquired - this is what
   *   actually guarantees stale connections get cleaned up even on quiet endpoints
   */
  @Bean
  fun defaultHttpConnectionProvider(): ConnectionProvider = ConnectionProvider.builder("default-http-pool")
    .maxIdleTime(MAX_IDLE_TIME)
    .maxLifeTime(MAX_LIFE_TIME)
    .evictInBackground(EVICTION_INTERVAL)
    .build()
    .also { HttpResources.set(it) }
}
