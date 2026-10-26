package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Controls whether scheduled background processing (inbox/outbox processing, case refresh worker,
 * monitors, clean-up jobs) actually runs.
 *
 * Scheduling infrastructure is always registered, but [EnabledAwareTaskScheduler] skips every
 * triggered task while [enabled] is false. The value is read on every trigger, so it can be toggled
 * at runtime (e.g. by integration tests that need processing) without creating a new Spring context.
 */
@ConfigurationProperties(prefix = "scheduling")
class SchedulingProperties(
  enabled: Boolean,
) {
  @Volatile
  var enabled: Boolean = enabled
}
