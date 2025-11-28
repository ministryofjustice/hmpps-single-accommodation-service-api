package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReferralTimingGuidanceRule : Rule {
  override val services = listOf(ServiceType.CAS1)
  override val description = "Referral should be completed 6 months prior to release date"

  override fun evaluate(data: DomainData): RuleResult {
    val now = LocalDate.now()
    val monthsUntilRelease = ChronoUnit.MONTHS.between(now, data.releaseDate)
    // marking as guidance status for now - maybe this should be flagged a different way?
    val ruleStatus = if (monthsUntilRelease < 6) RuleStatus.GUIDANCE else RuleStatus.PASS
    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }

  // additional logic needed for when to apply this rule - does this always need to be evaluated?
}
