package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAddressTypeNotPrivateRule : DtrEligibilityRule {
  override val description = "FAIL if current address is Private"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (ALLOWED_TYPES.contains(data.currentAccommodation?.type?.code)) RuleStatus.FAIL else RuleStatus.PASS,
  )

  companion object {
    private val ALLOWED_TYPES = setOf(
      "A07B", // Friends/Family (settled)
      "A07A", // Friends/Family (transient)
      "A01A", // Householder (Owner - freehold or leasehold)
      "A01C", // Rental accommodation - private rental
      "A01D", // Rental accommodation - social rental (LA or other)"
    )
  }
}
