package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class SexualOffenceRegistrationsRule : Rule {
  override val description = "FAIL if individual has sexual offence registrations."

  override fun evaluate(data: DomainData): RuleResult {
    val hasRegistrations = !data.sexualOffenceRegistrations?.sexualOffenceRegistrations.isNullOrEmpty()
    return RuleResult(
      description = description,
      ruleStatus = if (hasRegistrations) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (hasRegistrations) FailureReason.SEXUAL_OFFENCE_REGISTRATIONS_PRESENT else null,
    )
  }
}
