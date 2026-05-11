package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class RecentCurrentAccommodationEndDateRuleTest {
  private val description = "FAIL if not within 1 year of release from current accommodation"
  private val clock = MutableClock()

  @Test
  fun `candidate fails when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    val result = RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL))
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).minusDays(1)),
    )

    val result = RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is exactly 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).plusYears(1)),
    )

    val result = RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails currentAccommodationEndDate is more than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).plusDays(367)),
    )

    val result = RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL))
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is less than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).plusDays(364)),
    )

    val result = RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(RecentCurrentAccommodationEndDateRule(clock).description)
      .isEqualTo("FAIL if not within 1 year of release from current accommodation")
  }
}
