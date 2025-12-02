package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine

open class EligibilityBaseTest {
  protected val sTierRule = STierRule()
  protected val maleRiskRule = MaleRiskRule()
  protected val nonMaleRiskRule = NonMaleRiskRule()
  protected val withinSixMonthsOfReleaseRule = WithinSixMonthsOfReleaseRule()
  protected val cas1RuleSet = Cas1RuleSet(
    sTierRule,
    maleRiskRule,
    nonMaleRiskRule,
    withinSixMonthsOfReleaseRule,
  )
  protected val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()
  protected val defaultRulesEngine = RulesEngine(defaultRuleSetEvaluator)
  protected val circuitBreakRuleSetEvaluator = CircuitBreakRuleSetEvaluator()
  protected val circuitBreakRulesEngine = RulesEngine(circuitBreakRuleSetEvaluator)
  protected val male = Sex(
    code = SexCode.M,
    description = "Male",
  )
  protected val female = Sex(
    code = SexCode.F,
    description = "Female",
  )
}
