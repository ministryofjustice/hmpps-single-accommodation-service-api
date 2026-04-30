package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class IsMaleRule : CrsEligibilityRule {
  override val description = "FAIL if candidate is not male"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.sex != SexCode.M) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
