package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.Cas3RecentReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Cas3RecentReleaseDateRuleTest {
  private val crn = "ABC234"
  private val description = "FAIL if CAS3 application is not within 1 year of release"
  private val clock = MutableClock()

  @Test
  fun `release date is within 1 year so rule passes`() {
    val releaseDate = LocalDate.now()
    val data = buildDomainData(
      crn = crn,
      releaseDate = releaseDate,
    )

    clock.setNow(releaseDate.minusMonths(1))

    val result = Cas3RecentReleaseDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `release date is exactly a year from now so rule passes`() {
    val releaseDate = LocalDate.now()
    val data = buildDomainData(
      crn = crn,
      releaseDate = releaseDate,
    )

    clock.setNow(releaseDate.minusYears(1))

    val result = Cas3RecentReleaseDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `release date is more than a year from now so rule fails`() {
    val releaseDate = LocalDate.now()
    val data = buildDomainData(
      crn = crn,
      releaseDate = releaseDate,
    )

    clock.setNow(releaseDate.minusYears(2))

    val result = Cas3RecentReleaseDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `release date is in the past so rule passes`() {
    val releaseDate = LocalDate.now()
    val data = buildDomainData(
      crn = crn,
      releaseDate = releaseDate,
    )

    clock.setNow(releaseDate.plusDays(2))

    val result = Cas3RecentReleaseDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3RecentReleaseDateRule(clock).description).isEqualTo(description)
  }
}
