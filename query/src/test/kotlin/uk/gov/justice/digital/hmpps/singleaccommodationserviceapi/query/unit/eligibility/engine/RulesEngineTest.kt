package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
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
          AlwaysPassRule(),
          AlwaysPassRule(),
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
          AlwaysPassRule(),
          AlwaysPassRule(),
          AlwaysFailRule(),
          AlwaysPassRule(),
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
  fun `rules engine passes for an empty rule set`() {
    val result = execute()

    assertThat(result.status).isEqualTo(RuleSetStatus.PASS)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `rules engine retains all failed rule reasons in evaluation order`() {
    val result = execute(
      FixedOutcomeRule("First failure", RuleStatus.FAIL, FailureReason.UNDER_18),
      FixedOutcomeRule("Failure without a reason", RuleStatus.FAIL, null),
      AlwaysPassRule(),
      FixedOutcomeRule("Second failure", RuleStatus.FAIL, FailureReason.IN_COMMUNITY),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.UNDER_18, FailureReason.IN_COMMUNITY)
  }

  @Test
  fun `passing rules do not contribute failure reasons`() {
    val result = execute(
      FixedOutcomeRule("Passing rule with a reason", RuleStatus.PASS, FailureReason.UNDER_18),
      AlwaysFailRule(),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).containsExactly(FailureReason.IS_SETTLED)
  }

  @Test
  fun `rules engine fails without throwing when a failed rule has no reason`() {
    val result = execute(
      FixedOutcomeRule("Failure without a reason", RuleStatus.FAIL, null),
    )

    assertThat(result.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(result.failureReasons).isEmpty()
  }

  private fun execute(vararg rules: Rule) = defaultRulesEngine.execute(
    ruleset = StubRuleSet(rules.toList()),
    data = buildDomainData(),
  )
}
