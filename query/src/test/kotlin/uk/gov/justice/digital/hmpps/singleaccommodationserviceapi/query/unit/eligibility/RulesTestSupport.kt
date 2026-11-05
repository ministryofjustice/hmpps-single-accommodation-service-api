package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus

class StubRule(override val description: String) : Rule {
  override fun evaluate(data: DomainData) = RuleResult(description, RuleStatus.PASS)
}

class StubRuleSet(
  private val rules: List<Rule>,
  override val name: String = "ExampleEligibility",
) : RuleSet {
  override fun getRules(): List<Rule> = rules
}

class StubContextUpdater : ContextUpdater() {
  override fun toServiceResult(context: EvaluationContext) = context.currentResult!!.copy(
    serviceStatus = ServiceStatusNew.CAS1_UPCOMING,
  )
}

class AlwaysPassRule :
  FixedOutcomeRule(
    description = "FAIL never",
    ruleStatus = RuleStatus.PASS,
    failureReason = null,
  )

class AlwaysFailRule :
  FixedOutcomeRule(
    description = "FAIL always",
    ruleStatus = RuleStatus.FAIL,
    failureReason = FailureReason.IS_SETTLED,
  )

open class FixedOutcomeRule(
  override val description: String,
  val ruleStatus: RuleStatus,
  override val failureReason: FailureReason?,
) : Rule {
  var hasBeenEvaluated: Boolean = false

  override fun evaluate(data: DomainData): RuleResult {
    hasBeenEvaluated = true
    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
      failureReason = failureReason,
    )
  }
}
