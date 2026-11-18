package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface TelemetryService {
  fun trackEvent(
    name: String,
    properties: Map<String, String?> = emptyMap(),
    measurements: Map<String, Double?> = emptyMap(),
  )
}

@Service
class AppInsightsTelemetryService(
  private val telemetryClient: TelemetryClient = TelemetryClient(),
) : TelemetryService {
  override fun trackEvent(
    name: String,
    properties: Map<String, String?>,
    measurements: Map<String, Double?>,
  ) {
    runCatching {
      log.debug("Tracking telemetry event {}", name)
      telemetryClient.trackEvent(
        name,
        properties.withoutNullValues(),
        measurements.withoutNullValues(),
      )
    }.onFailure { log.warn("Failed to track telemetry event {}", name, it) }
  }

  private fun <V : Any> Map<String, V?>.withoutNullValues(): Map<String, V> = mapNotNull { (key, value) -> value?.let { key to it } }.toMap()

  private companion object {
    private val log = LoggerFactory.getLogger(AppInsightsTelemetryService::class.java)
  }
}
