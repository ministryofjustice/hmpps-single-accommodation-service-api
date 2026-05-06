package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAccommodationTypeRule : Cas3EligibilityRule {
  override val description = "FAIL if current accommodation is not Approved Premise (CAS1), CAS2, or Prison"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (ALLOWED_TYPES.contains(data.currentAccommodation?.type?.code)) {
      RuleStatus.PASS
    } else {
      RuleStatus.FAIL
    },
  )

  companion object {
    // TODO add in prison and bail type
    private val ALLOWED_TYPES = setOf(
      AccommodationTypeCode.A02, // CAS1
      AccommodationTypeCode.A10, // CAS2
      AccommodationTypeCode.A11, // CAS2
    )
  }
}
