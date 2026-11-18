package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.telemetry.TelemetryService
import java.util.concurrent.CopyOnWriteArrayList

@TestConfiguration
class TelemetryCaptureTestConfig {
  @Bean
  @Primary
  fun testTelemetryService() = TestTelemetryService()
}

class TestTelemetryService : TelemetryService {
  val events = CopyOnWriteArrayList<TrackedEvent>()

  override fun trackEvent(
    name: String,
    properties: Map<String, String?>,
    measurements: Map<String, Double?>,
  ) {
    events.add(TrackedEvent(name, properties, measurements))
  }

  fun reset() {
    events.clear()
  }

  data class TrackedEvent(
    val name: String,
    val properties: Map<String, String?>,
    val measurements: Map<String, Double?>,
  )
}
