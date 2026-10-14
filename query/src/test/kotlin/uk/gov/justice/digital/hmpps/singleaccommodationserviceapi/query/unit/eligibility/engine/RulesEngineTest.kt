package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysFailRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysPassRule
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
    assertThat(result.failureReasons).contains(
      FailureReason.IS_SETTLED,
    )
  }
}
