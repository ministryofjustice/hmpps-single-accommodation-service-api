package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine

@Service
class RulesService {

  fun calculateEligibilityForCas1(): FinalResult {
    // Loaded domain data (Out of scope for current ticket)
    val data = DomainData(
      tier = "A1",
    )

// 1. Set what evaluator we are going to use default is just a proxy
    val ruleEvaluator = DefaultRuleEvaluator()

// 2. Set what evaluator we are going to use default is just a proxy
    val ruleSetEvaluator = DefaultRuleSetEvaluator(ruleEvaluator)

// 3. get the ruleSet
    val ruleSet = Cas1RuleSet()

// 4. get the engine
    val engine = RulesEngine(ruleSetEvaluator)

// 5. run the ruleset
    return engine.execute(ruleSet, data)
  }
}
