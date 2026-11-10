package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class RejectedRequestRule : Rule<Cas1ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS1_REQUEST_FOR_PLACEMENT_STATUS)
  override val description = "FAIL if request for placement is rejected"

  override fun evaluate(data: Cas1ApplicationData): RuleResult {
    val isFail = data.cas1Application?.currentOutcome?.requestForPlacement?.status == Cas1RequestForPlacementStatus.REQUEST_REJECTED
    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
