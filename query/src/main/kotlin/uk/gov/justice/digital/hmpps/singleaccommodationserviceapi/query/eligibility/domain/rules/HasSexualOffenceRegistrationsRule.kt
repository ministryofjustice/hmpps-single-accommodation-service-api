package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class HasSexualOffenceRegistrationsRule : Rule {
  override val description = "FAIL if person has sexual offence registrations."
  override val failureReason = FailureReason.SEXUAL_OFFENCE_REGISTRATIONS_PRESENT

  override fun evaluate(data: DomainData): RuleResult {
    val hasRegistrations = !data.sexualOffenceRegistrations?.sexualOffenceRegistrations.isNullOrEmpty()
    return RuleResult(
      description = description,
      ruleStatus = if (hasRegistrations) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (hasRegistrations) failureReason else null,
    )
  }
}
