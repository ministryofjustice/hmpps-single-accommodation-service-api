package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinOneYearRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class ReleaseWithinOneYearRuleTest {
  private val clock = MutableClock()

  @Test
  fun `candidate passes when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    val result = ReleaseWithinOneYearRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val now = LocalDate.now(clock)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = now.minusDays(1)),
    )

    val result = ReleaseWithinOneYearRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is exactly 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val oneYearLater = LocalDate.now(clock).plusYears(1)
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = oneYearLater),
    )

    val result = ReleaseWithinOneYearRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails currentAccommodationEndDate is more than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val oneYearLater = LocalDate.now(clock).plusYears(1)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = oneYearLater.plusDays(1)),
    )

    val result = ReleaseWithinOneYearRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is less than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val oneYearLater = LocalDate.now(clock).plusYears(1)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = oneYearLater.minusDays(1)),
    )

    val result = ReleaseWithinOneYearRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(ReleaseWithinOneYearRule(clock).description)
      .isEqualTo("FAIL if not within 1 year of release from current accommodation")
  }
}
