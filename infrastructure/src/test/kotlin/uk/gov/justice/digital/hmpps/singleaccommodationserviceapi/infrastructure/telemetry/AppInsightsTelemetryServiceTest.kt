package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AppInsightsTelemetryServiceTest {

  @MockK
  lateinit var telemetryClient: TelemetryClient

  @InjectMockKs
  lateinit var telemetryService: AppInsightsTelemetryService

  @Test
  fun `tracks the event with its properties and measurements`() {
    every { telemetryClient.trackEvent(any(), any(), any()) } just runs

    telemetryService.trackEvent(
      name = "TestEvent",
      properties = mapOf("priority" to "LIVE"),
      measurements = mapOf("pendingDue" to 3.0),
    )

    verify { telemetryClient.trackEvent("TestEvent", mapOf("priority" to "LIVE"), mapOf("pendingDue" to 3.0)) }
  }

  @Test
  fun `drops null properties and measurements`() {
    every { telemetryClient.trackEvent(any(), any(), any()) } just runs

    telemetryService.trackEvent(
      name = "TestEvent",
      properties = mapOf("priority" to "LIVE", "failureCategory" to null),
      measurements = mapOf("pendingDue" to 3.0, "endToEndSeconds" to null),
    )

    verify { telemetryClient.trackEvent("TestEvent", mapOf("priority" to "LIVE"), mapOf("pendingDue" to 3.0)) }
  }

  @Test
  fun `does not throw when the telemetry client fails`() {
    every { telemetryClient.trackEvent(any(), any(), any()) } throws IllegalStateException("App Insights unavailable")

    assertThatCode { telemetryService.trackEvent(name = "TestEvent") }.doesNotThrowAnyException()
  }
}
