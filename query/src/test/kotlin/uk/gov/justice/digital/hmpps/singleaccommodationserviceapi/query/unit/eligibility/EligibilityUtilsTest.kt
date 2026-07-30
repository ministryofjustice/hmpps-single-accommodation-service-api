package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanOneYearInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInThePast
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class EligibilityUtilsTest {
  private val clock = MutableClock()

  @Nested
  inner class IsLessThanOneYearInTheFuture {
    @Test
    fun `Returns true when end date is missing`() {
      val today = LocalDate.now(clock)
      val result = isLessThanOneYearInTheFuture(null, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is within 1 year in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusYears(1).minusDays(1)
      val result = isLessThanOneYearInTheFuture(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is exactly 1 year in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusYears(1)
      val result = isLessThanOneYearInTheFuture(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when end date is more than 1 year in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusYears(1).plusDays(1)
      val result = isLessThanOneYearInTheFuture(endDate, today)
      assertThat(result).isFalse()
    }
  }

  @Nested
  inner class IsLessThanXWeeksInTheFuture {
    val numOfWeeks = 4L

    @Test
    fun `Returns true when end date is missing`() {
      val today = LocalDate.now(clock)
      val result = isLessThanXWeeksInTheFuture(null, today, numOfWeeks)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is within 4 weeks in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusWeeks(numOfWeeks).minusDays(1)
      val result = isLessThanXWeeksInTheFuture(endDate, today, numOfWeeks)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is exactly 4 weeks in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusWeeks(numOfWeeks)
      val result = isLessThanXWeeksInTheFuture(endDate, today, numOfWeeks)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when end date is more than 4 weeks in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusWeeks(numOfWeeks).plusDays(1)
      val result = isLessThanXWeeksInTheFuture(endDate, today, numOfWeeks)
      assertThat(result).isFalse()
    }
  }

  @Nested
  inner class IsLessThanXWeeksInThePast {
    val numOfWeeks = 12L

    @Test
    fun `Returns false when end date is missing`() {
      val today = LocalDate.now(clock)
      val result = isLessThanXWeeksInThePast(null, today, numOfWeeks)
      assertThat(result).isFalse()
    }

    @Test
    fun `Returns true when end date is within X weeks in the past`() {
      val today = LocalDate.now(clock)
      val endDate = today.minusWeeks(numOfWeeks).plusDays(1)
      val result = isLessThanXWeeksInThePast(endDate, today, numOfWeeks)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is exactly X weeks in the past`() {
      val today = LocalDate.now(clock)
      val endDate = today.minusWeeks(numOfWeeks)
      val result = isLessThanXWeeksInThePast(endDate, today, numOfWeeks)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when end date is more than X weeks in the past`() {
      val today = LocalDate.now(clock)
      val endDate = today.minusWeeks(numOfWeeks).minusDays(1)
      val result = isLessThanXWeeksInThePast(endDate, today, numOfWeeks)
      assertThat(result).isFalse()
    }
  }
}
