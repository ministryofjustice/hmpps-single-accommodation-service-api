package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webtestclient.autoconfigure.WebTestClientBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux

/**
 * Many tests only assert on the status/headers and never read the response body. Reactor Netty keeps such a
 * connection 'active' until the body is consumed, so unread bodies leak connections from the JVM-wide pool
 * (see ConnectionPoolConfig). Once the pool is exhausted every request waits and fails with
 * 'Timeout on blocking read for 5000000000 NANOSECONDS'.
 *
 * This buffers each response body up front so the connection is always released.
 */
@TestConfiguration
class TestWebTestClientConfig {
  @Bean
  fun drainResponseBodyCustomizer(): WebTestClientBuilderCustomizer = WebTestClientBuilderCustomizer { builder ->
    builder.filter(
      ExchangeFilterFunction.ofResponseProcessor { response ->
        response.bodyToMono<ByteArray>()
          .defaultIfEmpty(ByteArray(0))
          .map { bytes ->
            response.mutate()
              .body(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(bytes)))
              .build()
          }
      },
    )
  }
}
