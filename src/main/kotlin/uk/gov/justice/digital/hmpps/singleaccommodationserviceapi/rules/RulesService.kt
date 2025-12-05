package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1StatusRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime

@Service
class RulesService {
  private val ruleSetEvaluator = DefaultRuleSetEvaluator()
  private val engine = RulesEngine(ruleSetEvaluator)
  private val cas1StatusRuleSet = Cas1StatusRuleSet()
  private val cas1EligibilityRuleSet = Cas1EligibilityRuleSet()

  fun calculateResultForCas1(crn: String): ServiceResult {
    val data = buildDomainData(crn)
    return runRulesEngineForCas1(data)
  }

  fun runRulesEngineForCas1(data: DomainData): ServiceResult {
    val statusRuleSetResult = engine.execute(cas1StatusRuleSet, data)
    return buildResults(
      statusRuleSetResult,
      data,
      ServiceStatus.CONFIRMED,
      ServiceStatus.SUBMITTED,
      cas1StatusFailFunc,
    )
  }

  private val cas1StatusFailFunc = fun (
    failedResults: List<RuleResult>,
    data: DomainData,
  ): ServiceResult {
    val eligibilityRuleset = engine.execute(cas1EligibilityRuleSet, data)
    return buildResults(
      eligibilityRuleset,
      data,
      ServiceStatus.UPCOMING,
      ServiceStatus.NOT_STARTED,
      cas1EligibilityFailFunc,
    )
  }

  private val cas1EligibilityFailFunc = fun (failedResults: List<RuleResult>, data: DomainData) = ServiceResult(
    serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    actions = listOf(),
    failedResults = failedResults,
  )

  fun buildResults(
    ruleSetResult: RuleSetResult,
    data: DomainData,
    passStatus: ServiceStatus,
    guidanceFailStatus: ServiceStatus,
    failFunc: (failedResults: List<RuleResult>, data: DomainData) -> ServiceResult,
  ): ServiceResult {
    val failedResults = ruleSetResult.results.filter { it.ruleStatus == RuleStatus.FAIL }
    val actions = ruleSetResult.results.filter { it.potentialAction != null }.map { it.potentialAction!! }
    return when (ruleSetResult.ruleSetStatus) {
      RuleSetStatus.FAIL -> failFunc(failedResults, data)
      RuleSetStatus.PASS -> ServiceResult(
        serviceStatus = passStatus,
        actions = actions,
        failedResults = listOf(),
      )
      RuleSetStatus.GUIDANCE_FAIL -> ServiceResult(
        serviceStatus = guidanceFailStatus,
        actions = actions,
        failedResults = failedResults,
      )
    }
  }

  private fun buildDomainData(crn: String) = DomainData(
    tier = "A1",
    sex = Sex(
      code = "M",
      description = "Male",
    ),
    releaseDate = OffsetDateTime.now().plusMonths(6),
    cas1Application = null,
  )
}
