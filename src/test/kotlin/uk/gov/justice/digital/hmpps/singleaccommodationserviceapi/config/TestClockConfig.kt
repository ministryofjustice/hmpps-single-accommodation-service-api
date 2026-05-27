package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

@TestConfiguration
class TestClockConfig {
  @Bean
  @Primary
  fun testClock(): MutableTestClock = MutableTestClock()
}

class MutableTestClock : Clock() {
  private val zoneRef = AtomicReference<ZoneId>(ZoneOffset.UTC)
  private val fixedInstantRef = AtomicReference<Instant?>(null)

  override fun getZone(): ZoneId = zoneRef.get()

  override fun withZone(zone: ZoneId): Clock {
    zoneRef.set(zone)
    return this
  }

  override fun instant(): Instant = fixedInstantRef.get() ?: system(zoneRef.get()).instant()

  fun freezeAt(instant: Instant) {
    fixedInstantRef.set(instant)
  }

  fun reset() {
    fixedInstantRef.set(null)
    zoneRef.set(ZoneOffset.UTC)
  }
}
