package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import java.time.LocalDate
import java.util.UUID

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityRuleSet::class,
    STierEligibilityRule::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    Cas1SuitabilityRuleSet::class,
    ApplicationSuitabilityRule::class,
    Cas1CompletionRuleSet::class,
    ApplicationCompletionRule::class,
    ClockConfig::class,
    DefaultRuleSetEvaluator::class,
    CircuitBreakRuleSetEvaluator::class,
  ],
)
class RuleSetEvaluatorTest {
  @Autowired
  lateinit var cas1CompletionRuleSet: Cas1CompletionRuleSet

  @Autowired
  lateinit var cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet

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
  val applicationSuitabilityRuleDescription = "FAIL if candidate does not have a suitable application"
  val applicationCompletionRuleDescription = "FAIL if application is not complete"

  fun buildStTierRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: RuleAction? = null) = RuleResult(
    description = stTierRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildMaleRiskRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: RuleAction? = null) = RuleResult(
    description = maleRiskRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildNonMaleRiskRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: RuleAction? = null) = RuleResult(
    description = nonMaleRiskRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildApplicationSuitabilityRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: RuleAction? = null) = RuleResult(
    description = applicationSuitabilityRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  fun buildApplicationCompletionRuleResult(ruleStatus: RuleStatus, actionable: Boolean, potentialAction: RuleAction? = null) = RuleResult(
    description = applicationCompletionRuleDescription,
    ruleStatus = ruleStatus,
    actionable = actionable,
    potentialAction = potentialAction,
  )

  @Nested
  inner class DefaultRuleSetEvaluatorTests {

    @Nested
    inner class Cas1EligibilityRuleSetTests {
      @Test
      fun `default rule set evaluator everything passes (male)`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.A1,
          sex = buildSex(SexCode.M),
          releaseDate = LocalDate.now().plusMonths(7),
        )

        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildStTierRuleResult(RuleStatus.PASS, false),
          buildMaleRiskRuleResult(RuleStatus.PASS, false),
          buildNonMaleRiskRuleResult(RuleStatus.PASS, false),
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
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildStTierRuleResult(RuleStatus.FAIL, false),
          buildMaleRiskRuleResult(RuleStatus.PASS, false),
          buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
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
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildStTierRuleResult(RuleStatus.FAIL, false),
          buildMaleRiskRuleResult(RuleStatus.PASS, false),
          buildNonMaleRiskRuleResult(RuleStatus.PASS, false),
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
        val result = defaultRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

        val expectedResult = listOf(
          buildStTierRuleResult(RuleStatus.PASS, false),
          buildMaleRiskRuleResult(RuleStatus.PASS, false),
          buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
        )

        assertThat(result).isEqualTo(expectedResult)
      }
    }

    @Nested
    inner class Cas1SuitabilityRuleSetTests {

      @Test
      fun `default rule set evaluator everything passes`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.A1,
          sex = buildSex(SexCode.M),
          releaseDate = LocalDate.now().plusMonths(7),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
            placementStatus = null
          )
        )

        val result = defaultRuleSetEvaluator.evaluate(cas1SuitabilityRuleSet, data)

        val expectedResult = listOf(
          buildApplicationSuitabilityRuleResult(RuleStatus.PASS, true),
        )

        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `default rule set evaluator everything fails`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.C2S,
          sex = buildSex(SexCode.F),
          releaseDate = LocalDate.now().plusMonths(2),
        )
        val result = defaultRuleSetEvaluator.evaluate(cas1SuitabilityRuleSet, data)

        val expectedResult = listOf(
          buildApplicationSuitabilityRuleResult(RuleStatus.FAIL, true, RuleAction("Start approved premise referral")),
        )

        assertThat(result).isEqualTo(expectedResult)
      }
    }

    @Nested
    inner class Cas1CompletionRuleSetTests {

      @Test
      fun `default rule set evaluator everything passes`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.A1,
          sex = buildSex(SexCode.M),
          releaseDate = LocalDate.now().plusMonths(7),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = Cas1PlacementStatus.UPCOMING,
          )
        )

        val result = defaultRuleSetEvaluator.evaluate(cas1CompletionRuleSet, data)

        val expectedResult = listOf(
          buildApplicationCompletionRuleResult(RuleStatus.PASS, true),
        )

        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `default rule set evaluator everything fails`() {
        val data = DomainData(
          crn = crn,
          tier = TierScore.C2S,
          sex = buildSex(SexCode.F),
          releaseDate = LocalDate.now().plusMonths(2),
        )
        val result = defaultRuleSetEvaluator.evaluate(cas1CompletionRuleSet, data)

        val expectedResult = listOf(
          buildApplicationCompletionRuleResult(RuleStatus.FAIL, true),
        )

        assertThat(result).isEqualTo(expectedResult)
      }
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

      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

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
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

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
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

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
      val result = circuitBreakRuleSetEvaluator.evaluate(cas1EligibilityRuleSet, data)

      val expectedResult = listOf(
        buildNonMaleRiskRuleResult(RuleStatus.FAIL, false),
      )

      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
