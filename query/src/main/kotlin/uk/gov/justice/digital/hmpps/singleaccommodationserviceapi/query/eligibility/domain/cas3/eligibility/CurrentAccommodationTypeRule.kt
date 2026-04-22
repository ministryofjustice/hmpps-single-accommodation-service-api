package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAccommodationTypeRule : Cas3EligibilityRule {
  override val description = "FAIL if current accommodation is not Approved Premise (CAS1), CAS2, or Prison"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.currentAccommodation != null && data.currentAccommodation.isPrisonCas1Cas2OrCas2v2) {
      RuleStatus.PASS
    } else {
      RuleStatus.FAIL
    },
  )
}
