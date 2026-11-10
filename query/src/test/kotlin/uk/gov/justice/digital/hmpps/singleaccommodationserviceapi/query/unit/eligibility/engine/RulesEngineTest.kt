package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.ReleaseOutside15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas2DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysFailRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.AlwaysPassRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.FixedOutcomeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class RulesEngineTest {
  private val defaultRulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  @Test
  fun `a shared ruleset evaluates different service-specific input types`() {
    val clock = Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC)
    val releaseWithin15Days = ReleaseWithin15DaysRuleSet(ReleaseOutside15DaysRule(clock))
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.of(2026, 12, 1)),
    )

    val cas2 = defaultRulesEngine.execute(releaseWithin15Days, data.toCas2DomainData())
    val cas3 = defaultRulesEngine.execute(releaseWithin15Days, data.toCas3DomainData())

    assertThat(cas2.status).isEqualTo(RuleSetStatus.FAIL)
    assertThat(cas3).isEqualTo(cas2)
  }

  @Test
  fun `typed tree accepts shared rulesets and updaters without losing its service input type`() {
    val clock = Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"), ZoneOffset.UTC)
    val builder = DecisionTreeBuilder<Cas2DomainData>(defaultRulesEngine)
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.of(2026, 12, 1)),
    ).toCas2DomainData()
    val initialContext = EvaluationContext(data, currentResult = null)
    val updater = Cas2UpcomingContextUpdater()
    val updatedContext: EvaluationContext<Cas2DomainData> = updater.update(initialContext)
    val tree = builder.ruleSet(ReleaseWithin15DaysRuleSet(ReleaseOutside15DaysRule(clock)))
      .onPass(builder.outcome(buildServiceResultNew(ServiceStatusNew.CAS2_NOT_STARTED)))
      .onFail(builder.confirmed()).applyContextUpdaterOnFailure(updater)
      .build()

    assertThat(updatedContext.data).isSameAs(data)
    assertThat(tree.eval(initialContext)).isEqualTo(
      buildServiceResultNew(ServiceStatusNew.CAS2_UPCOMING, actionStartDate = LocalDate.of(2026, 11, 16)),
    )
  }

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

  private fun execute(vararg rules: Rule<DomainData>) = defaultRulesEngine.execute(
    ruleset = StubRuleSet(rules.toList()),
    data = buildDomainData(),
  )
}
