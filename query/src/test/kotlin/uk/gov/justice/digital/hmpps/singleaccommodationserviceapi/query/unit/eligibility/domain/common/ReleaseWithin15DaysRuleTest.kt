package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class ReleaseWithin15DaysRuleTest {
  private val clock = MutableClock()

  @Test
  fun `person passes when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    val result = ReleaseWithin15DaysRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val now = LocalDate.now(clock)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = now.minusDays(1)),
    )

    val result = ReleaseWithin15DaysRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person passes currentAccommodationEndDate is exactly 15 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fifteenDaysLater = LocalDate.now(clock).plusDays(15)
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fifteenDaysLater),
    )

    val result = ReleaseWithin15DaysRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person fails currentAccommodationEndDate is more than 15 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fifteenDaysLater = LocalDate.now(clock).plusDays(15)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fifteenDaysLater.plusDays(1)),
    )

    val result = ReleaseWithin15DaysRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `person passes currentAccommodationEndDate is less than 15 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))
    val fifteenDaysLater = LocalDate.now(clock).plusDays(15)

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = fifteenDaysLater.minusDays(1)),
    )

    val result = ReleaseWithin15DaysRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    Assertions.assertThat(ReleaseWithin15DaysRule(clock).description)
      .isEqualTo("FAIL if the current accommodation end date is more than 15 days from today")
  }
}
