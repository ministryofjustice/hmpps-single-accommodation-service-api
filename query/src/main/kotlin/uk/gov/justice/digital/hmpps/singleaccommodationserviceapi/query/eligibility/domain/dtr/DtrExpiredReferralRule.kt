package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInThePast
import java.time.Clock
import java.time.LocalDate

@Component
class DtrExpiredReferralRule(val clock: Clock) : Rule {
  override val description = "FAIL if DTR is submitted more than 26 weeks ago."

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (
      isLessThanXWeeksInThePast(data.dutyToRefer?.submission?.submissionDate, LocalDate.now(clock), 26)
    ) {
      RuleStatus.PASS
    } else {
      RuleStatus.FAIL
    },
  )
}
