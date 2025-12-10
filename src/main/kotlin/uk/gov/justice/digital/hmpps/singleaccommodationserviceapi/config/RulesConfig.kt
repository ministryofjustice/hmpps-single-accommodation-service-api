package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine

@Configuration
class RulesConfig {
  @Bean
  fun defaultRulesEngine(): RulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  @Bean
  fun circuitBreakerRulesEngine(): RulesEngine = RulesEngine(CircuitBreakRuleSetEvaluator())

  @Bean
  fun cas1RuleSet(): Cas1RuleSet = Cas1RuleSet(
    STierRule(),
    MaleRiskRule(),
    NonMaleRiskRule(),
    WithinSixMonthsOfReleaseRule(),
  )
}
