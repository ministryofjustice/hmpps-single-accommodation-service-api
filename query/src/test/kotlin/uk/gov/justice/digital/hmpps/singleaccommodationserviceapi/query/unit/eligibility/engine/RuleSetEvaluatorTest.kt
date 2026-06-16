package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
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
    failureReason = if (ruleStatus == RuleStatus.FAIL) FailureReason.S_TIER else null,
  )

  fun buildMaleRiskRuleResult(ruleStatus: RuleStatus) = RuleResult(
    description = maleRiskRuleDescription,
    ruleStatus = ruleStatus,
    failureReason = if (ruleStatus == RuleStatus.FAIL) FailureReason.MALE_NOT_HIGH_RISK_TIER else null,
  )

  fun buildNonMaleRiskRuleResult(ruleStatus: RuleStatus) = RuleResult(
    description = nonMaleRiskRuleDescription,
    ruleStatus = ruleStatus,
    failureReason = if (ruleStatus == RuleStatus.FAIL) FailureReason.NON_MALE_NOT_HIGH_RISK_TIER else null,
  )

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

    @Test
    fun `default rule set evaluator everything passes (male)`() {
      val data = buildDomainData(
        crn = crn,
        tierScore = "A1",
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
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
      val data = buildDomainData(
        crn = crn,
        tierScore = "C2S",
        sex = SexCode.F,
        currentAccommodation = null,
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
      val data = buildDomainData(
        crn = crn,
        tierScore = "A1S",
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
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
      val data = buildDomainData(
        crn = crn,
        tierScore = "C1",
        sex = SexCode.F,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(3)),
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
      val data = buildDomainData(
        crn = crn,
        tierScore = "A1",
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
      )

      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf<RuleResult>()

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set nearly evaluator everything fails`() {
      val data = buildDomainData(
        crn = crn,
        tierScore = "A1S",
        sex = SexCode.F,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildSTierRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first fails, second passes`() {
      val data = buildDomainData(
        crn = crn,
        tierScore = "A1S",
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildSTierRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `circuit breaker rule set evaluator first passes, second fails`() {
      val data = buildDomainData(
        crn = crn,
        tierScore = "C1",
        sex = SexCode.F,
        currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusMonths(7)),
      )
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildNonMaleRiskRuleResult(RuleStatus.FAIL),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
