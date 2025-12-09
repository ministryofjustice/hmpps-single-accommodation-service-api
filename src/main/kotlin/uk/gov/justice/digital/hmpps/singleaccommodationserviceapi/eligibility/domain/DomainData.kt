package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine
import java.time.OffsetDateTime

data class DomainData(
  val crn: String,
  val tier: String,
  val sex: Sex,
  val releaseDate: OffsetDateTime,
  val cas1Application: Cas1Application? = null,
) {
  private val ruleSetEvaluator = DefaultRuleSetEvaluator()
  private val engine = RulesEngine(ruleSetEvaluator)

  fun calculateEligibility(ruleSet: RuleSet): ServiceResult {
    val ruleSetResult = engine.execute(ruleSet, this)
    val actions = ruleSetResult.results.filter { it.potentialAction != null }.map { it.potentialAction!! }
    return when (ruleSetResult.ruleSetStatus) {
      RuleSetStatus.FAIL -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        actions = listOf(),
      )
      RuleSetStatus.PASS -> ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        actions = actions,
      )
      RuleSetStatus.ACTION_NEEDED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        actions = actions,
      )
    }
  }
}
