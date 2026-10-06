package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.netty.http.HttpResources

class ConnectionPoolConfigTest {

  @Test
  fun `defaultHttpConnectionProvider installs itself as Reactor Netty's JVM-wide default pool`() {
    val provider = ConnectionPoolConfig().defaultHttpConnectionProvider()

    assertThat(provider.name()).isEqualTo("default-http-pool")
    assertThat(HttpResources.get().name()).isEqualTo("default-http-pool")
  }
}
