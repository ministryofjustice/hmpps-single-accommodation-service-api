package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CurrentAccommodationTypeRule : Rule {
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
      "A02", // CAS1
      "A10", // CAS2
      "A11", // CAS2
    )
  }
}
