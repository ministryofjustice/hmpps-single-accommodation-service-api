package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class MutableClock(private var fixedTime: Instant? = null) : Clock() {

  fun setNow(date: LocalDate) {
    fixedTime = date.atStartOfDay().toInstant(ZoneOffset.UTC)
  }

  fun reset() {
    fixedTime = null
  }

  override fun instant(): Instant = fixedTime ?: Instant.now()

  override fun getZone(): ZoneId = ZoneOffset.UTC

  override fun withZone(zone: ZoneId?): Clock = this
}