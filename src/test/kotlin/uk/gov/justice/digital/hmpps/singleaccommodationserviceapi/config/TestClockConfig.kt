package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@TestConfiguration
class TestClockConfig {

  @Bean
  fun clock(): TestClock = TestClock()
}

class TestClock : Clock() {

  private var delegate: Clock = systemUTC()

  fun set(instant: Instant) {
    delegate = fixed(instantTruncatedToMicroSeconds(instant), ZoneOffset.UTC)
  }

  fun reset() {
    delegate = systemUTC()
  }

  override fun getZone(): ZoneId = delegate.zone

  override fun withZone(zone: ZoneId): Clock = fixed(delegate.instant(), zone)

  override fun instant(): Instant = delegate.instant()
}

private fun instantTruncatedToMicroSeconds(instant: Instant? = null) = (instant ?: Instant.now()).truncatedTo(ChronoUnit.MILLIS)
