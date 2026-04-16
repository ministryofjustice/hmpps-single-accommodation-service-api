package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRule
import java.time.Clock
import java.time.LocalDate

@Component
class DtrExpiredReferralRule(val clock: Clock) :
  Cas3EligibilityRule,
  DtrSuitabilityRule {
  override val description = "FAIL if DTR is submitted more than 180 days ago."

  override fun evaluate(data: DomainData): RuleResult {
    if (data.dtrSubmissionDate == null) {
      return RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    }

    return RuleResult(
      description = description,
      ruleStatus = if (data.dtrSubmissionDate < LocalDate.now(clock).minusDays(180)) RuleStatus.FAIL else RuleStatus.PASS,
    )
  }
}
