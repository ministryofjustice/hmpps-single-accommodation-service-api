package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class NoConflictingCas1BookingRule : Cas3EligibilityRule {
  override val description = "FAIL if CAS1 booking exists for upcoming release"

  override fun evaluate(data: DomainData): RuleResult {
    val hasConflictingCas1Booking = data.cas1Application?.applicationStatus == Cas1ApplicationStatus.PLACEMENT_ALLOCATED &&
      data.cas1Application.placementStatus == Cas1PlacementStatus.UPCOMING &&
      data.cas1Application.requestForPlacementStatus == Cas1RequestForPlacementStatus.PLACEMENT_BOOKED

    return RuleResult(
      description = description,
      ruleStatus = if (hasConflictingCas1Booking) RuleStatus.FAIL else RuleStatus.PASS,
    )
  }
}
