package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RulesService {
  private val ruleSetEvaluator = DefaultRuleSetEvaluator()
  private val engine = RulesEngine(ruleSetEvaluator)
  private val cas1EligibilityRuleSet = Cas1EligibilityRuleSet()

  fun calculateResultForCas1(crn: String): ServiceResult {
    val cas1Application = getSuitableCas1Application(crn)
    if (cas1Application != null) {
      return buildStatusServiceResult(cas1Application)
    } else {
      val data = buildDomainData(crn)
      val eligibilityRuleSetResult = engine.execute(cas1EligibilityRuleSet, data)
      return buildEligibilityServiceResult(eligibilityRuleSetResult)
    }
  }

  fun buildStatusServiceResult(cas1Application: Cas1Application) = ServiceResult(
    failedResults = listOf(),
    serviceStatus = cas1Application.status.toString(),
    actions = listOf(),
  )

  fun buildEligibilityServiceResult(ruleSetResult: RuleSetResult): ServiceResult {
    val failedResults = ruleSetResult.results.filter { it.ruleStatus == RuleStatus.FAIL }
    val actions = ruleSetResult.results.filter { it.potentialAction != null }.map { it.potentialAction!! }
    return when (ruleSetResult.ruleSetStatus) {
      RuleSetStatus.FAIL -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_ELIGIBLE.toString(),
        actions = listOf(),
        failedResults = failedResults,
      )
      RuleSetStatus.PASS -> ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING.toString(),
        actions = actions,
        failedResults = listOf(),
      )
      RuleSetStatus.ACTION_NEEDED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED.toString(),
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
  )

  private fun getSuitableCas1Application(crn: String): Cas1Application? = Cas1Application(
    id = UUID.randomUUID(),
  )
}
