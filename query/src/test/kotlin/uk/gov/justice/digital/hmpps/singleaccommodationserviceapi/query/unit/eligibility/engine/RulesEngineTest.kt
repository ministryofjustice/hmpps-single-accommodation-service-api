package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysFailRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysPassRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.FixedOutcomeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet

class RulesEngineTest {
  private val defaultRulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  @Test
  fun `rules engine passes if all rules pass`() {
    val data = buildDomainData()

    val result = defaultRulesEngine.execute(
      ruleset = StubRuleSet(
        listOf(
          PrioritisedRule(
            AlwaysPassRule(),
          ),
          PrioritisedRule(

            AlwaysPassRule(),
          ),
        ),
      ),
      data = data,
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.PASS)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `rules engine fails if one rule fails`() {
    val data = buildDomainData()

    val result = defaultRulesEngine.execute(
      ruleset = StubRuleSet(
        listOf(
          PrioritisedRule(
            AlwaysPassRule(),
          ),
          PrioritisedRule(
            AlwaysPassRule(),
          ),
          PrioritisedRule(
            AlwaysFailRule(),
            0,
          ),
          PrioritisedRule(
            AlwaysPassRule(),
          ),
        ),
      ),
      data = data,
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(
      FailureReason.IS_SETTLED,
    )
  }

  @Test
  fun `rules engine returns only the lowest priority failure reasons`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("Higher priority number", RuleStatus.FAIL, FailureReason.IS_SETTLED), 3),
      PrioritisedRule(FixedOutcomeRule("Lowest priority number", RuleStatus.FAIL, FailureReason.UNDER_18), 0),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.UNDER_18)
  }

  @Test
  fun `rules engine retains all reasons tied at the lowest priority in evaluation order`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("First tied failure", RuleStatus.FAIL, FailureReason.UNDER_18), 0),
      PrioritisedRule(FixedOutcomeRule("Lower precedence failure", RuleStatus.FAIL, FailureReason.IS_SETTLED), 3),
      PrioritisedRule(FixedOutcomeRule("Second tied failure", RuleStatus.FAIL, FailureReason.IN_COMMUNITY), 0),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.UNDER_18, FailureReason.IN_COMMUNITY)
  }

  @Test
  fun `rules engine excludes priority minus one before selecting the lowest priority`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("Excluded failure", RuleStatus.FAIL, FailureReason.IS_SETTLED), -1),
      PrioritisedRule(FixedOutcomeRule("Relevant failure", RuleStatus.FAIL, FailureReason.UNDER_18), 2),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.UNDER_18)
  }

  @Test
  fun `rules engine fails with no reasons when all failures have priority minus one`() {
    val result = execute(PrioritisedRule(AlwaysFailRule(), -1))

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `rules engine passes for an empty rule set`() {
    val result = execute()

    assertThat(result.status).isEqualTo(RuleSetStatus.PASS)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `passing rules do not affect failure reason priority selection`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("Passing rule with a reason", RuleStatus.PASS, FailureReason.UNDER_18), 0),
      PrioritisedRule(AlwaysFailRule(), 3),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.IS_SETTLED)
  }

  @Test
  fun `rules engine fails without throwing when a failed rule has no reason`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("Failure without a reason", RuleStatus.FAIL, null), 0),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `a reasonless failure suppresses lower precedence failure reasons`() {
    val result = execute(
      PrioritisedRule(FixedOutcomeRule("Failure without a reason", RuleStatus.FAIL, null), 0),
      PrioritisedRule(FixedOutcomeRule("Lower precedence failure", RuleStatus.FAIL, FailureReason.IS_SETTLED), 1),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).isEmpty()
  }

  private fun execute(vararg rules: PrioritisedRule) = defaultRulesEngine.execute(
    ruleset = StubRuleSet(rules.toList()),
    data = buildDomainData(),
  )
}
