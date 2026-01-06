package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAddressTypeRule : Cas3EligibilityRule {
  override val description = "FAIL if current address is not Approved Premise (CAS1), CAS2, or Prison"

  private val eligibleAccommodationTypes = setOf(
    AccommodationArrangementType.PRISON,
    AccommodationArrangementType.CAS1,
    AccommodationArrangementType.CAS2,
    AccommodationArrangementType.CAS2V2,
  )

  override fun evaluate(data: DomainData): RuleResult {
    val currentType = data.currentAccommodation?.arrangementType

    val isEligible = currentType != null && eligibleAccommodationTypes.contains(currentType)

    return RuleResult(
      description = description,
      ruleStatus = if (isEligible) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
