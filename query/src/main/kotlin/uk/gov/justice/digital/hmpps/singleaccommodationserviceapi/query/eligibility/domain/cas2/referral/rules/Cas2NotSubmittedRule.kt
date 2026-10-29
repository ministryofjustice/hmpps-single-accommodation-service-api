package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas2NotSubmittedRule : Rule {
  override val description = "FAIL if the application is not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val isFail = data.cas2Application?.submittedApplication?.submittedAt == null

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
