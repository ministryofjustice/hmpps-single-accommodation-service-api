package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas1ApplicationSuitabilityRule : Rule {
  override val description = "FAIL if candidate does not have a suitable application"

  override fun evaluate(data: DomainData): RuleResult {
    val unsuitableStatuses = listOf(
      Cas1ApplicationStatus.STARTED,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.EXPIRED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.WITHDRAWN,
    )

    val isFail = unsuitableStatuses.contains(data.cas1Application?.applicationStatus) && data.cas1Application?.requestForPlacementStatus == null && data.cas1Application?.placementStatus == null

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
