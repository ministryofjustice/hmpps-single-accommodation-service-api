package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime

@Service
class RulesService {

  fun calculateEligibilityForCas1(crn: String): FinalResult {
    // Loaded domain data (Out of scope for current ticket)
    val data = buildDomainData(crn)

// 1. Set what evaluator we are going to use default is just a proxy
    val ruleSetEvaluator = DefaultRuleSetEvaluator()

// 2. get the ruleSet
    val ruleSet = Cas1RuleSet()

// 3. get the engine
    val engine = RulesEngine(ruleSetEvaluator)

// 4. run the ruleset
    return engine.execute(ruleSet, data)
  }

  private fun buildDomainData(crn: String) = DomainData(
    tier = "A1",
    sex = Sex(
      code = "M",
      description = "Male",
    ),
    referralDate = null,
    releaseDate = OffsetDateTime.now().plusMonths(6),
  )
}
