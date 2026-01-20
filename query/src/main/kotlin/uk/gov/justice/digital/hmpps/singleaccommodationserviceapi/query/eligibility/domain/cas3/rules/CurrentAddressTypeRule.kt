package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAddressTypeRule : Cas3EligibilityRule {
  override val description = "FAIL if current address is not Approved Premise (CAS1), CAS2, or Prison"

  private val eligibleAccommodationTypes = setOf(
    AccommodationType.PRISON,
    AccommodationType.CAS1,
    AccommodationType.CAS2,
    AccommodationType.CAS2V2,
  )

  override fun evaluate(data: DomainData): RuleResult {
    val currentType = data.currentAccommodation?.type

    val isEligible = currentType != null && eligibleAccommodationTypes.contains(currentType)

    return RuleResult(
      description = description,
      ruleStatus = if (isEligible) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
