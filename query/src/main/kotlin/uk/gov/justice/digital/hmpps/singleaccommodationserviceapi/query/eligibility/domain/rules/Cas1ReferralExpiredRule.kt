package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas1ReferralExpiredRule : Rule<Cas1ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS1_APPLICATION_STATUS, DomainDataField.CAS1_PLACEMENT_STATUS)
  override val description = "FAIL if the referral is expired and there is no pending or active placement"

  override fun evaluate(data: Cas1ApplicationData): RuleResult {
    val placementStatus = data.cas1Application?.currentOutcome?.placement?.status
    val isFail = data.cas1Application?.application?.status == Cas1ApplicationStatus.EXPIRED &&
      (placementStatus != Cas1PlacementStatus.UPCOMING && placementStatus != Cas1PlacementStatus.ARRIVED)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
