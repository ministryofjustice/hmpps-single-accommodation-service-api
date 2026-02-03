package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime


private const val WORKING_DAY_START = 9
private const val WORKING_DAY_END = 17

fun ZonedDateTime.minusWorkingHours(hours: Int): ZonedDateTime {
  var result = this
  var remaining = hours

  while (remaining > 0) {
    val hoursAvailableToday = maxOf(0, result.hour - WORKING_DAY_START)

    if (hoursAvailableToday >= remaining) {
      return result.minusHours(remaining.toLong())
    }

    remaining -= hoursAvailableToday
    result = result.toLocalDate()
      .previousWorkingDay()
      .atTime(LocalTime.of(WORKING_DAY_END, 0))
      .atZone(result.zone)
  }

  return result
}

private fun LocalDate.previousWorkingDay(): LocalDate {
  var day = this.minusDays(1)
  while (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) {
    day = day.minusDays(1)
  }
  return day
}
