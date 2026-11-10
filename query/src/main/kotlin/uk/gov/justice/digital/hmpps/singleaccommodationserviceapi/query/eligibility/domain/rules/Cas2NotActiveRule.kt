package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas2NotActiveRule : Rule {
  override val description = "FAIL if assessment is present and inactive"

  override fun evaluate(data: DomainData): RuleResult {
    val unsuitableStatuses = listOf(
      Cas2AssessmentStatus.WITHDRAWN,
      Cas2AssessmentStatus.CANCELLED,
      Cas2AssessmentStatus.OFFER_DECLINED,
    )

    val isFail = unsuitableStatuses.contains(data.cas2Application?.submittedApplication?.latestAssessmentStatus)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
