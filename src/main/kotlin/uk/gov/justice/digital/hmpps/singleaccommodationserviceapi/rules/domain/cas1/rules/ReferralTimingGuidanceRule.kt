package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class ReferralTimingGuidanceRule : Rule {
  override val services = listOf(ServiceType.CAS1)
  override val description = "FAIL if candidate is within 6 months of release date"
  override val isGuidance = true

  override fun evaluate(data: DomainData): RuleResult {
    val monthsUntilRelease = ChronoUnit.MONTHS.between(
      OffsetDateTime.now().toLocalDate(),
      data.releaseDate.toLocalDate(),
    )

    val ruleStatus = if (monthsUntilRelease < 6) RuleStatus.FAIL else RuleStatus.PASS
    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
      isGuidance = isGuidance,
    )
  }
}
