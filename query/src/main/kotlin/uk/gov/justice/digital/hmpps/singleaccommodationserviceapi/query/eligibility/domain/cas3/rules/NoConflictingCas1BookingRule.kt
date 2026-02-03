package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

private val completedCas1PlacementStatuses = listOf(
  Cas1PlacementStatus.UPCOMING,
  Cas1PlacementStatus.ARRIVED,
)

@Component
class NoConflictingCas1BookingRule : Cas3EligibilityRule {
  override val description = "FAIL if CAS1 booking exists for upcoming release"

  override fun evaluate(data: DomainData): RuleResult {
    val hasConflictingCas1Booking = data.cas1Application?.applicationStatus == Cas1ApplicationStatus.PLACEMENT_ALLOCATED
      && data.cas1Application.placementStatus in completedCas1PlacementStatuses

    return RuleResult(
      description = description,
      ruleStatus = if (hasConflictingCas1Booking) RuleStatus.FAIL else RuleStatus.PASS,
    )
  }
}
