package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.ReleaseWithinFourWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class ReleaseWithinFourWeeksRuleTest {
  private val clock = MutableClock()

  @Test
  fun `candidate passes when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    val result = ReleaseWithinFourWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val now = LocalDate.now(clock)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = now.minusDays(1)),
    )

    val result = ReleaseWithinFourWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is exactly 4 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fourWeeksLater = LocalDate.now(clock).plusWeeks(4)
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fourWeeksLater),
    )

    val result = ReleaseWithinFourWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails currentAccommodationEndDate is more than 4 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fourWeeksLater = LocalDate.now(clock).plusWeeks(4)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fourWeeksLater.plusDays(1)),
    )

    val result = ReleaseWithinFourWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is less than 4 weeks in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fourWeeksLater = LocalDate.now(clock).plusWeeks(4)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fourWeeksLater.minusDays(1)),
    )

    val result = ReleaseWithinFourWeeksRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(ReleaseWithinFourWeeksRule(clock).description)
      .isEqualTo("FAIL if not within 4 weeks of release from current accommodation")
  }
}
