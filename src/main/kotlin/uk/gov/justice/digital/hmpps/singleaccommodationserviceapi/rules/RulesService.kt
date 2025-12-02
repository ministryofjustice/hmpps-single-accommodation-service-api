package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS

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
    return buildCas1Results(ruleSetResult, data)
  }

  private fun buildCas1Results(ruleSetResult: RuleSetResult, data: DomainData): ServiceResult {
    val actionText = "Start approved premise referral"
    var action = ""
    var cas1RuleSetStatus = ServiceStatus.NOT_ELIGIBLE
    if (ruleSetResult.ruleSetStatus == RuleSetStatus.PASS) {
      cas1RuleSetStatus = ServiceStatus.NOT_STARTED
      action = "$actionText ${getDaysUntilReleaseDateString(data.releaseDate)}"
    } else if (ruleSetResult.ruleSetStatus == RuleSetStatus.GUIDANCE_FAIL) {
      cas1RuleSetStatus = ServiceStatus.UPCOMING
      action = actionText
    }
    return ServiceResult(
      serviceStatus = cas1RuleSetStatus,
      action = action,
      failedResults = ruleSetResult.failedResults,
    )
  }

  private fun getDaysUntilReleaseDateString(releaseDate: OffsetDateTime): String {
    val daysUntilRelease = DAYS.between(OffsetDateTime.now(), releaseDate).toInt()
    return if (daysUntilRelease == 0) {
      "in 1 day"
    } else if (daysUntilRelease == 1) {
      "in 1 day"
    } else if (daysUntilRelease > 1) {
      "in $daysUntilRelease days"
    } else {
      throw error("Days until release is negative, status should be Cas1RuleSetStatus.NOT_STARTED")
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
