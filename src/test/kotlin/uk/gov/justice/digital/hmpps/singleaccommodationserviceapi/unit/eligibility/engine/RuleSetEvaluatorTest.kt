package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityBaseTest
import java.time.OffsetDateTime

class RuleSetEvaluatorTest : EligibilityBaseTest() {
  private val crn = "ABC234"

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

    @Test
    fun `default rule set evaluator everything passes (male)`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )

      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.PASS, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(nonMaleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(withinSixMonthsOfReleaseRule.description, RuleStatus.PASS, true, "Start approved premise referral in 31 days"),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator nearly everything fails (female)`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C2S,
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(2),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(nonMaleRiskRule.description, RuleStatus.FAIL, false),
        RuleResult(withinSixMonthsOfReleaseRule.description, RuleStatus.FAIL, true, "Start approved premise referral"),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first fails, second passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(nonMaleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(withinSixMonthsOfReleaseRule.description, RuleStatus.PASS, true, "Start approved premise referral in 31 days"),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first passes, second fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C1,
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(3),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.PASS, false),
        RuleResult(maleRiskRule.description, RuleStatus.PASS, false),
        RuleResult(nonMaleRiskRule.description, RuleStatus.FAIL, false),
        RuleResult(withinSixMonthsOfReleaseRule.description, RuleStatus.FAIL, true, "Start approved premise referral"),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }

  @Nested
  inner class CircuitBreakerRuleSetEvaluatorTests {

    @Test
    fun `circuit breaker rule set evaluator everything passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )

      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf<RuleResult>()

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set nearly evaluator everything fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, second passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C1,
        sex = female,
        releaseDate = OffsetDateTime.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        RuleResult(nonMaleRiskRule.description, RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
