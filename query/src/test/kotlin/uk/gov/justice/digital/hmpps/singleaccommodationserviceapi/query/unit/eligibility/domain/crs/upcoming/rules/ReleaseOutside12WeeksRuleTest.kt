package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.upcoming.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.rules.ReleaseOutside12WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ReleaseOutside12WeeksRuleTest {
  private val clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
  private val rule = ReleaseOutside12WeeksRule(clock)
  private val description = "FAIL if release is more than 12 weeks from today"

  @ParameterizedTest
  @ValueSource(longs = [-1, 0, 83, 84])
  fun `release in the past or within 12 weeks passes including the boundary`(daysFromToday: Long) {
    val result = rule.evaluate(
      buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).plusDays(daysFromToday)),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `release one day beyond 12 weeks fails`() {
    val result = rule.evaluate(
      buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now(clock).plusWeeks(12).plusDays(1)),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing release date passes`() {
    val result = rule.evaluate(
      buildDomainData(currentAccommodation = buildAccommodationSummaryDto(endDate = null)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing current accommodation passes`() {
    val result = rule.evaluate(buildDomainData(currentAccommodation = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
