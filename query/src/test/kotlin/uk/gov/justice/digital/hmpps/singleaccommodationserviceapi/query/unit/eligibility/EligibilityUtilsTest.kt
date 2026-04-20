package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildUpcomingAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithinOneYear
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class EligibilityUtilsTest {
  private val clock = MutableClock()

  @Nested
  inner class IsWithinOneYear {
    @Test
    fun `Returns true when end date and today are within 1 year`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusDays(3))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is earlier than today`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.plusDays(3))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when end date is exactly one year from today`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusYears(1))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(endDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when end date is more than one year from today`() {
      val endDate = LocalDate.parse("2026-12-31")
      clock.setNow(endDate.minusDays(400))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(endDate, today)
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
