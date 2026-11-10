package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrReferralData

@Component
class DtrNotPresentRule : Rule<DtrReferralData> {
  override val inputFields = setOf(DomainDataField.DTR_STATUS)
  override val description = "FAIL if DTR status is not present"

  override fun evaluate(data: DtrReferralData) = RuleResult(
    description = description,
    ruleStatus = if (data.dutyToRefer?.status == null) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
