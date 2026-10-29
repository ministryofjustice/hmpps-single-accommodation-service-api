package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutsideEightWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class ReleaseOutsideEightWeeksRuleTest {
  private val clock = MutableClock()

  @Test
  fun `person passes when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    val result = ReleaseOutsideEightWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val now = LocalDate.now(clock)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = now.minusDays(1)),
    )

    val result = ReleaseOutsideEightWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person passes currentAccommodationEndDate is exactly 8 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val eightWeeksLater = LocalDate.now(clock).plusWeeks(8)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = eightWeeksLater),
    )

    val result = ReleaseOutsideEightWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person fails currentAccommodationEndDate is more than 8 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val eightWeeksLater = LocalDate.now(clock).plusWeeks(8)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = eightWeeksLater.plusDays(1)),
    )

    val result = ReleaseOutsideEightWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `person passes currentAccommodationEndDate is less than 8 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val eightWeeksLater = LocalDate.now(clock).plusWeeks(8)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = eightWeeksLater.minusDays(1)),
    )

    val result = ReleaseOutsideEightWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(ReleaseOutsideEightWeeksRule(clock).description)
      .isEqualTo("FAIL if release is more than 8 weeks from today")
  }
}
