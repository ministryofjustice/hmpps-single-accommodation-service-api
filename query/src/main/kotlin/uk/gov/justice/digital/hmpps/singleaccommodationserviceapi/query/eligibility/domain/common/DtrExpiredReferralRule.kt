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
  override val description = "FAIL if DTR is submitted more than 26 weeks ago."

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (
      data.dutyToRefer?.submission?.submissionDate == null ||
      data.dutyToRefer.submission!!.submissionDate < LocalDate.now(clock).minusWeeks(26)
    ) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
  )
}
