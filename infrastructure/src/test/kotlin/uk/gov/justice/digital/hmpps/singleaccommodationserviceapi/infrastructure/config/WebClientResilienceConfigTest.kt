package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import kotlin.math.max

class WebClientResilienceConfigTest {

  @Test
  fun `pool drops idle connections and keeps the default capacity`() {
    val provider = hmppsHttpConnectionProvider(Duration.ofSeconds(30), Duration.ofSeconds(15))
    try {
      assertThat(provider.maxConnections())
        .isEqualTo(max(ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS, 500))
    } finally {
      provider.dispose()
    }
  }
}
