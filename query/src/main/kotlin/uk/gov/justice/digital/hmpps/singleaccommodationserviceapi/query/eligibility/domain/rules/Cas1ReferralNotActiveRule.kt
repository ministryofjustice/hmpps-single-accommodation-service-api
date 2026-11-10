package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas1ReferralNotActiveRule : Rule<Cas1ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS1_APPLICATION_STATUS)
  override val description = "FAIL if the referral is not active"

  override fun evaluate(data: Cas1ApplicationData): RuleResult {
    val active = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
    )

    val isPass = active.contains(data.cas1Application?.application?.status)

    val ruleStatus = if (isPass) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
