package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityRuleSet::class,
    STierEligibilityRule::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    ClockConfig::class,
    DefaultRuleSetEvaluator::class,
    CircuitBreakRuleSetEvaluator::class,
  ],
)
class RuleSetEvaluatorTest {
  @Autowired
  lateinit var cas1EligibilityRuleSet: Cas1EligibilityRuleSet

  @Autowired
  lateinit var defaultRuleSetEvaluator: DefaultRuleSetEvaluator

  @Autowired
  lateinit var circuitBreakRuleSetEvaluator: CircuitBreakRuleSetEvaluator

  private val crn = "ABC234"
  val stTierRuleDescription = "FAIL if candidate is S Tier"
  val maleRiskRuleDescription = "FAIL if candidate is Male and is not Tier A3 - B1"
  val nonMaleRiskRuleDescription = "FAIL if candidate is not Male and is not Tier A3 - C3"

  fun buildSTierRuleResult(ruleStatus: RuleStatus) = RuleResult(
    description = stTierRuleDescription,
    ruleStatus = ruleStatus,
  )

  fun buildMaleRiskRuleResult(ruleStatus: RuleStatus) = RuleResult(
    description = maleRiskRuleDescription,
    ruleStatus = ruleStatus,
  )

  fun buildNonMaleRiskRuleResult(ruleStatus: RuleStatus) = RuleResult(
    description = nonMaleRiskRuleDescription,
    ruleStatus = ruleStatus,
  )

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

      @Test
      fun `default rule set evaluator everything passes (male)`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.A1,
          sex = SexCode.M,
          releaseDate = LocalDate.now().plusMonths(7),
        )

        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildSTierRuleResult(RuleStatus.PASS),
          buildMaleRiskRuleResult(RuleStatus.PASS),
          buildNonMaleRiskRuleResult(RuleStatus.PASS),
        )

        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `default rule set evaluator nearly everything fails (female)`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.C2S,
          sex = SexCode.F,
          releaseDate = null,
        )
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildSTierRuleResult(RuleStatus.FAIL),
          buildMaleRiskRuleResult(RuleStatus.PASS),
          buildNonMaleRiskRuleResult(RuleStatus.FAIL),
        )

        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `default rule set evaluator first fails, second passes`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.A1S,
          sex = SexCode.M,
          releaseDate = LocalDate.now().plusMonths(7),
        )
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildSTierRuleResult(RuleStatus.FAIL),
          buildMaleRiskRuleResult(RuleStatus.PASS),
          buildNonMaleRiskRuleResult(RuleStatus.PASS),
        )

        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `default rule set evaluator first passes, second fails`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.C1,
          sex = SexCode.F,
          releaseDate = LocalDate.now().plusMonths(3),
        )
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildSTierRuleResult(RuleStatus.PASS),
          buildMaleRiskRuleResult(RuleStatus.PASS),
          buildNonMaleRiskRuleResult(RuleStatus.FAIL),
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
        sex = SexCode.M,
        releaseDate = LocalDate.now().plusMonths(7),
      )

      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf<RuleResult>()

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set nearly evaluator everything fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = SexCode.F,
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildSTierRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, second passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = SexCode.M,
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildSTierRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C1,
        sex = SexCode.F,
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildNonMaleRiskRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
