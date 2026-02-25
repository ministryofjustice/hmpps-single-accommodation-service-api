package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.util.minusWorkingHours
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WorkingHoursTest {

  private val zone = ZoneId.of("Europe/London")

  // All test days are created from a single default 'Monday' date
  private val monday = LocalDate.of(2025, 1, 6)
  private val tuesday = monday.plusDays(1)
  private val wednesday = monday.plusDays(2)
  private val friday = monday.plusDays(4)
  private val previousFriday = monday.minusDays(3)
  private val previousWednesday = monday.minusDays(5)

  init {
    // fail if default monday date is ever changed to a day that isn't a Monday
    require(monday.dayOfWeek == DayOfWeek.MONDAY) { "Default 'monday' date must be a Monday" }
  }

  private fun atHour(date: LocalDate, hour: Int): ZonedDateTime = ZonedDateTime.of(date, LocalTime.of(hour, 0), zone)

  @Nested
  inner class MinusWorkingHours {

    @Test
    fun `returns same time when subtracting zero hours`() {
      val wednesday2pm = atHour(wednesday, 14)

      val result = wednesday2pm.minusWorkingHours(0)

      assertThat(result).isEqualTo(wednesday2pm)
    }

    @Test
    fun `subtracts hours within the same working day`() {
      val result = atHour(wednesday, 14).minusWorkingHours(3)

      assertThat(result).isEqualTo(atHour(wednesday, 11))
    }

    @Test
    fun `subtracts hours that exactly reach start of working day`() {
      val result = atHour(wednesday, 14).minusWorkingHours(5)

      assertThat(result).isEqualTo(atHour(wednesday, 9))
    }

    @Test
    fun `rolls back to previous working day when hours exceed available today`() {
      val result = atHour(wednesday, 14).minusWorkingHours(6)

      assertThat(result).isEqualTo(atHour(tuesday, 16))
    }

    @Test
    fun `rolls back across multiple working days`() {
      val result = atHour(wednesday, 14).minusWorkingHours(13)

      assertThat(result).isEqualTo(atHour(tuesday, 9))
    }

    @Test
    fun `skips weekends when rolling back from Monday`() {
      val result = atHour(monday, 14).minusWorkingHours(6)

      assertThat(result).isEqualTo(atHour(previousFriday, 16))
    }

    @Test
    fun `skips weekends when rolling back multiple days from Monday`() {
      val result = atHour(monday, 14).minusWorkingHours(13)

      assertThat(result).isEqualTo(atHour(previousFriday, 9))
    }

    @Test
    fun `handles subtracting a full working week`() {
      val result = atHour(friday, 17).minusWorkingHours(40)

      assertThat(result).isEqualTo(atHour(monday, 9))
    }

    @Test
    fun `handles start time at beginning of working day`() {
      val result = atHour(wednesday, 9).minusWorkingHours(1)

      assertThat(result).isEqualTo(atHour(tuesday, 16))
    }

    @Test
    fun `handles start time at end of working day`() {
      val result = atHour(wednesday, 17).minusWorkingHours(8)

      assertThat(result).isEqualTo(atHour(wednesday, 9))
    }

    @Test
    fun `handles start time before working hours`() {
      val result = atHour(wednesday, 7).minusWorkingHours(2)

      assertThat(result).isEqualTo(atHour(tuesday, 15))
    }

    @Test
    fun `spans across multiple weekends`() {
      val result = atHour(wednesday, 14).minusWorkingHours(45)

      assertThat(result).isEqualTo(atHour(previousWednesday, 9))
    }
  }
}
