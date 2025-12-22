package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.CircuitBreakRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine

@Configuration
class RulesConfig {
  @Bean
  fun defaultRulesEngine(): RulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  @Bean
  fun circuitBreakerRulesEngine(): RulesEngine = RulesEngine(CircuitBreakRuleSetEvaluator())
}
