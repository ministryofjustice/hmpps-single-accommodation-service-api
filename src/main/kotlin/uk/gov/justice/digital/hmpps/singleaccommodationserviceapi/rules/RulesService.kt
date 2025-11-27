package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime

@Service
class RulesService {

  fun calculateEligibilityForCas1(crn: String): ServiceResult {
    // Loaded domain data (Out of scope for current ticket)
    val data = buildDomainData(crn)

// 1. Set what evaluator we are going to use default is just a proxy
    val ruleSetEvaluator = DefaultRuleSetEvaluator()

// 2. get the ruleSet
    val ruleSet = Cas1RuleSet()

// 3. get the engine
    val engine = RulesEngine(ruleSetEvaluator)

// 4. run the ruleset
    val ruleSetResult = engine.execute(ruleSet, data)

// build results
    return buildCas1Results(ruleSetResult)
  }

  fun buildCas1Results(ruleSetResult: RuleSetResult): ServiceResult {
    val failedResults = ruleSetResult.results.filter { it.ruleStatus == RuleStatus.FAIL }
    val actions = ruleSetResult.results.filter { it.potentialAction != null }.map { it.potentialAction!! }
    return when (ruleSetResult.ruleSetStatus) {
      RuleSetStatus.FAIL -> {
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          actions = listOf(),
          failedResults = failedResults,
        )
      }
      RuleSetStatus.PASS -> {
        ServiceResult(
          serviceStatus = ServiceStatus.UPCOMING,
          actions = actions,
          failedResults = listOf(),
        )
      }
      RuleSetStatus.GUIDANCE_FAIL -> {
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          actions = actions,
          failedResults = failedResults,
        )
      }
    }
  }

  private fun buildDomainData(crn: String) = DomainData(
    tier = "A1",
    sex = Sex(
      code = "M",
      description = "Male",
    ),
    releaseDate = OffsetDateTime.now().plusMonths(6),
  )
}
