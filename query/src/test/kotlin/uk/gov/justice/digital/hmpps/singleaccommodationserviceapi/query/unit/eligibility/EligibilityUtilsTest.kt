package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildUpcomingAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThan56DaysInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanOneYearInTheFuture
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInThePast
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class EligibilityUtilsTest {
  private val clock = MutableClock()

  @Nested
  inner class IsLessThanOneYearInTheFuture {
    @Test
    fun `Returns false when end date is missing`() {
      val today = LocalDate.now(clock)
      val result = isLessThanOneYearInTheFuture(null, today)
      assertThat(result).isFalse()
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
  inner class IsLessThan56DaysInTheFuture {
    @Test
    fun `Returns true when end date is missing`() {
      val today = LocalDate.now(clock)
      val result = isLessThan56DaysInTheFuture(null, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is within 56 days in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusDays(56).minusDays(1)
      val result = isLessThan56DaysInTheFuture(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is exactly 56 days in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusDays(56)
      val result = isLessThan56DaysInTheFuture(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when end date is more than 56 days in the future`() {
      val today = LocalDate.now(clock)
      val endDate = today.plusDays(56).plusDays(1)
      val result = isLessThan56DaysInTheFuture(endDate, today)
      assertThat(result).isFalse()
    }
  }

  @Nested
  inner class IsLessThan12weeksInThePast {
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

  @Nested
  inner class BuildUpcomingAction {
    @Test
    fun `Build action when end date is 3 days in future`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusDays(3))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(endDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when end date is 13 months in future`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusMonths(13))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(endDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 31 days (31 December 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when end date is 1 year in future`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusYears(1))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(endDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when end date is 1 year and 1 day in future`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusYears(1).minusDays(1))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(endDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 1 day (31 December 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when end date is 1 year and 2 days in future`() {
      val endDate = LocalDate.parse("2026-07-01")
      clock.setNow(endDate.minusYears(1).minusDays(2))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(endDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days (1 July 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
