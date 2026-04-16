package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrRecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrRecentCurrentAccommodationEndDateRuleTest {
  private val clock = MutableClock()

  @Test
  fun `candidate passes when currentAccommodationEndDate are missing`() {
    val data = buildDomainData(
      releaseDate = null,
      currentAccommodationEndDate = null,
    )

    val result = DtrRecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is in the past`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodationEndDate = LocalDate.now(clock).minusDays(1),
    )

    val result = DtrRecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is exactly 56 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodationEndDate = LocalDate.now(clock).plusDays(56),
    )

    val result = DtrRecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails currentAccommodationEndDate is more than 56 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodationEndDate = LocalDate.now(clock).plusDays(57),
    )

    val result = DtrRecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes currentAccommodationEndDate is less than 56 days in the future`() {
    clock.setNow(LocalDate.parse("2025-01-01"))

    val data = buildDomainData(
      currentAccommodationEndDate = LocalDate.now(clock).plusDays(50),
    )

    val result = DtrRecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrRecentCurrentAccommodationEndDateRule(clock).description)
      .isEqualTo("FAIL if not within 56 days of release from current accommodation")
  }
}
