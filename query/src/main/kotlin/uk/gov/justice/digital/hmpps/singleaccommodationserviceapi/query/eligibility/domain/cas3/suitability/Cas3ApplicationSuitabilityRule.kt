package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3ApplicationSuitabilityRule : Cas3SuitabilityRule {
  override val description = "FAIL if CAS3 application is rejected"

  override fun evaluate(data: DomainData): RuleResult {
    val ruleStatus = if (
      data.cas3Application?.bookingStatus == null &&
      data.cas3Application?.assessmentStatus == null &&
      data.cas3Application?.applicationStatus == Cas3ApplicationStatus.REJECTED
    ) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    }

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
