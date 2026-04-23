package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Cas3RecentCurrentAccommodationEndDateRuleTest {
  private val clock = MutableClock()

  @Test
  fun `candidate fails when currentAccommodationEndDate is missing`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(endDate = null),
    )

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(endDate = LocalDate.now(clock).minusDays(1)),
    )

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is exactly 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(endDate = LocalDate.now(clock).plusYears(1)),
    )

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails currentAccommodationEndDate is more than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(endDate = LocalDate.now(clock).plusDays(367)),
    )

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is less than 1 year in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(endDate = LocalDate.now(clock).plusDays(364)),
    )

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3RecentCurrentAccommodationEndDateRule(clock).description)
      .isEqualTo("FAIL if not within 1 year of release from current accommodation")
  }
}
