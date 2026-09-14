package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas2SuitableStatusRule : Rule {
  override val description = "FAIL if candidate has an unsuitable status"

  override fun evaluate(data: DomainData): RuleResult {
    val unsuitableStatuses = listOf(
      "withdrawn",
      "cancelled",
      "offerDeclined",
    )

    val isFail = unsuitableStatuses.contains(data.cas2Application?.submittedApplication?.latestAssessmentStatus)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
