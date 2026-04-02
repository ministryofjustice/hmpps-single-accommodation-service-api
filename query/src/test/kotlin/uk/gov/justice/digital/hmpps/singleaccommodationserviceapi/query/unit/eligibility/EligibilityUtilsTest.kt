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
    fun `Returns true when release date and today are within 1 year`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(releaseDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when release date is earlier than today`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.plusDays(3))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(releaseDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns true when release date is exactly one year from today`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusYears(1))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(releaseDate, today)
      assertThat(result).isTrue()
    }

    @Test
    fun `Returns false when release date is more than one year from today`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(400))
      val today = LocalDate.now(clock)
      val result = isWithinOneYear(releaseDate, today)
      assertThat(result).isFalse()
    }
  }

  @Nested
  inner class BuildUpcomingAction {
    @Test
    fun `Build action when release date is 3 days in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(releaseDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 13 months in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(13))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(releaseDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 31 days (31 December 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusYears(1))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(releaseDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = EligibilityKeys.START_APPROVED_PREMISE_APPLICATION
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year and 1 day in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusYears(1).minusDays(1))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(releaseDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 1 day (31 December 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year and 2 days in future`() {
      val releaseDate = LocalDate.parse("2026-07-01")
      clock.setNow(releaseDate.minusYears(1).minusDays(2))
      val today = LocalDate.now(clock)
      val result = buildUpcomingAction(releaseDate, today, EligibilityKeys.START_APPROVED_PREMISE_APPLICATION)
      val expectedResult = "${EligibilityKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days (1 July 2025)"
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
