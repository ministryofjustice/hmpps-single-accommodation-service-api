package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas2NotActiveRule : Rule {
  override val description = "FAIL if assessment is present and not active"

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
