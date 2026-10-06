package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.netty.http.HttpResources
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

class ConnectionPoolConfigTest {

  @Test
  fun `defaultHttpConnectionProvider installs configured provider as Reactor Netty's JVM-wide default pool`() {
    val originalProvider = currentDefaultProvider()

    try {
      val provider = ConnectionPoolConfig().defaultHttpConnectionProvider()
      val installedProvider = currentDefaultProvider()

      assertThat(installedProvider).isSameAs(provider)
      assertThat(installedProvider.name()).isEqualTo("default-http-pool")
      assertThat(readProviderDurationSetting(installedProvider, "maxIdleTime")).isEqualTo(Duration.ofSeconds(30))
      assertThat(readProviderDurationSetting(installedProvider, "maxLifeTime")).isEqualTo(Duration.ofMinutes(5))
      assertThat(readProviderDurationSetting(installedProvider, "evictionInterval")).isEqualTo(Duration.ofSeconds(30))
    } finally {
      HttpResources.set(originalProvider)
    }
  }

  private fun currentDefaultProvider(): ConnectionProvider = HttpResources.get().readField("defaultProvider") as ConnectionProvider

  private fun readProviderDurationSetting(
    provider: ConnectionProvider,
    settingName: String,
  ): Duration {
    val builder = provider.readField("builder")
    return builder.readField(settingName) as Duration
  }

  private fun Any.readField(fieldName: String): Any {
    var type: Class<*>? = javaClass

    while (type != null) {
      runCatching {
        val field = type.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(this)
      }
      type = type.superclass
    }

    error("Field '$fieldName' not found on ${javaClass.name}")
  }
}
