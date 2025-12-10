package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
) {
  private val cas1RuleSet = Cas1RuleSet()
  private val ruleSetEvaluator = DefaultRuleSetEvaluator()
  private val engine = RulesEngine(ruleSetEvaluator)

  fun getEligibility(crn: String): EligibilityDto {
    val data = eligibilityOrchestrationService.getData(crn)

    val cas1 = if (data.cas1Application != null) {
      ServiceResult(
        serviceStatus = data.cas1Application.transformToServiceStatus(),
        actions = data.cas1Application.buildActions(),
      )
    } else {
      engine.execute(cas1RuleSet, data)
    }

    return EligibilityDto(
      crn,
      cas1,
    )
  }
}
