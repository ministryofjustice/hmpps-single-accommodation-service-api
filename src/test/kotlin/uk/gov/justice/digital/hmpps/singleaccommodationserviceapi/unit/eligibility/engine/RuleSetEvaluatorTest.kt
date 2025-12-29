package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1RuleSet::class,
    STierRule::class,
    MaleRiskRule::class,
    NonMaleRiskRule::class,
    WithinSixMonthsOfReleaseRule::class,
    ClockConfig::class,
    DefaultRuleSetEvaluator::class,
    CircuitBreakRuleSetEvaluator::class,
  ],
)
class RuleSetEvaluatorTest {
  @Autowired
  lateinit var cas1RuleSet: Cas1RuleSet

  @Autowired
  lateinit var defaultRuleSetEvaluator: DefaultRuleSetEvaluator

  @Autowired
  lateinit var circuitBreakRuleSetEvaluator: CircuitBreakRuleSetEvaluator

  private val crn = "ABC234"
  val stTierRuleDescription = "FAIL if candidate is S Tier"
  val maleRiskRuleDescription = "FAIL if candidate is Male and is not Tier A3 - B1"
  val nonMaleRiskRuleDescription = "FAIL if candidate is not Male and is not Tier A3 - C3"
  val withinSixMonthsRuleDescription = "FAIL if candidate is within 6 months of release date"

  fun buildStTierRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: String? = null) = RuleResult(
    description = stTierRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildMaleRiskRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: String? = null) = RuleResult(
    description = maleRiskRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildNonMaleRiskRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: String? = null) = RuleResult(
    description = nonMaleRiskRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildWithinSixMonthsRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: String? = null) = RuleResult(
    description = withinSixMonthsRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

    @Test
    fun `default rule set evaluator everything passes (male)`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = buildSex(SexCode.M),
        releaseDate = LocalDate.now().plusMonths(7),
      )

      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.PASS, false),
        buildMaleRiskRuleResult(RuleStatus.PASS, false),
        buildNonMaleRiskRuleResult(RuleStatus.PASS, false),
        buildWithinSixMonthsRuleResult(
          RuleStatus.PASS,
          true,
          "Start approved premise referral in 31 days",
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator nearly everything fails (female)`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C2S,
        sex = buildSex(SexCode.F),
        releaseDate = LocalDate.now().plusMonths(2),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.FAIL, false),
        buildMaleRiskRuleResult(RuleStatus.PASS, false),
        buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
        buildWithinSixMonthsRuleResult(
          RuleStatus.FAIL,
          true,
          "Start approved premise referral",
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first fails, second passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = buildSex(SexCode.M),
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.FAIL, false),
        buildMaleRiskRuleResult(RuleStatus.PASS, false),
        buildNonMaleRiskRuleResult(RuleStatus.PASS, false),
        buildWithinSixMonthsRuleResult(
          RuleStatus.PASS,
          true,
          "Start approved premise referral in 31 days",
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `default rule set evaluator first passes, second fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C1,
        sex = buildSex(SexCode.F),
        releaseDate = LocalDate.now().plusMonths(3),
      )
      val result = defaultRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.PASS, false),
        buildMaleRiskRuleResult(RuleStatus.PASS, false),
        buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
        buildWithinSixMonthsRuleResult(
          RuleStatus.FAIL,
          true,
          "Start approved premise referral",
        ),
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
        sex = buildSex(SexCode.M),
        releaseDate = LocalDate.now().plusMonths(7),
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
        sex = buildSex(SexCode.F),
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, second passes`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1S,
        sex = buildSex(SexCode.M),
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildStTierRuleResult(RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.C1,
        sex = buildSex(SexCode.F),
        releaseDate = LocalDate.now().plusMonths(7),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1RuleSet, data)

      val expectedResult = listOf(
        buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
