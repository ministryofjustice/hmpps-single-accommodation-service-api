package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3AssessmentSuitabilityRule : Cas3SuitabilityRule {
  override val description = "FAIL if CAS3 assessment is rejected or closed"

  override fun evaluate(data: DomainData): RuleResult {
    val ruleStatus = if (data.cas3Application?.bookingStatus == null && FAILING_ASSESSMENT_STATUSES.contains(data.cas3Application?.assessmentStatus)) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }

  companion object {
    private val FAILING_ASSESSMENT_STATUSES = setOf(
      Cas3AssessmentStatus.CLOSED,
      Cas3AssessmentStatus.REJECTED,
    )
  }
}
