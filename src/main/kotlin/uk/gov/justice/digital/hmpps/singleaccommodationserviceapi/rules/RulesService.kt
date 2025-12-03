package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration.RulesOrchestrationService

@Service
class RulesService(
  val rulesOrchestrationService: RulesOrchestrationService,
) {

  fun calculateEligibilityForCas1(crn: String): ServiceResult {
    // Loaded domain data (Out of scope for current ticket)
    val data = getDomainData(crn)

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

  fun buildDomainData(cpr: CorePersonRecord, tier: Tier, prisoner: Prisoner) = DomainData(cpr, tier, prisoner)

  fun getPrisonerNumberFromCprData(crn: String, cpr: CorePersonRecord): String {
    val prisonNumbers = cpr.identifiers?.prisonNumbers
    if (prisonNumbers.isNullOrEmpty()) {
      error("No prisoner number found for crn $crn")
    }
    return prisonNumbers.last()
  }

  fun getDomainData(crn: String): DomainData {
    val tierAndCprData = rulesOrchestrationService.getCprAndTier(crn)
    val prisoner = rulesOrchestrationService.getPrisoner(getPrisonerNumberFromCprData(crn, tierAndCprData.cpr))

    return buildDomainData(tierAndCprData.cpr, tierAndCprData.tier, prisoner)
  }
}
