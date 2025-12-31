package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.enums.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class WithinSixMonthsOfReleaseRuleTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)
  private val clock = MutableClock()

  @Test
  fun `no release date exists so passes`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = null,
    )
    val result = WithinSixMonthsOfReleaseRule(clock).evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
    assertThat(result.actionable).isFalse()
    assertThat(result.potentialAction).isNull()
  }

  @Test
  fun `release date within 6 months so fails`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(4),
    )
    val result = WithinSixMonthsOfReleaseRule(clock).evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `release date more than 6 months away so passes`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(7),
    )
    val result = WithinSixMonthsOfReleaseRule(clock).evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `Within 6 months of release so fails`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
    )
    val result = WithinSixMonthsOfReleaseRule(clock).evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `More than 6 months before release so passes`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(8),
    )
    val result = WithinSixMonthsOfReleaseRule(clock).evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(WithinSixMonthsOfReleaseRule(clock).description).isEqualTo("FAIL if candidate is within 6 months of release date")
  }

  @Nested
  inner class BuildAction {
    @Test
    fun `Build action when release date is 3 days in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = WithinSixMonthsOfReleaseRule(clock).buildAction(true, data)
      val expectedResult = "Start approved premise referral"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 7 months in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(7))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = WithinSixMonthsOfReleaseRule(clock).buildAction(false, data)
      val expectedResult = "Start approved premise referral in 30 days"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(6))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = WithinSixMonthsOfReleaseRule(clock).buildAction(false, data)
      val expectedResult = "Start approved premise referral"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 1 day in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(6).minusDays(1))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = WithinSixMonthsOfReleaseRule(clock).buildAction(false, data)
      val expectedResult = "Start approved premise referral in 1 day"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 2 days in future`() {
      val releaseDate = LocalDate.parse("2026-07-01")
      clock.setNow(releaseDate.minusMonths(6).minusDays(2))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = WithinSixMonthsOfReleaseRule(clock).buildAction(false, data)
      val expectedResult = "Start approved premise referral in 2 days"
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
