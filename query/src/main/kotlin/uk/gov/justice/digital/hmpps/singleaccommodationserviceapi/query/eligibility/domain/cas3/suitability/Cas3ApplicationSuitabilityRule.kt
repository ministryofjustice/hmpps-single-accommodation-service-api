package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3ApplicationSuitabilityRule : Rule {
  override val description = "FAIL if the CAS3 application is rejected or in progress and has neither a booking status nor an assessment status"

  override fun evaluate(data: DomainData): RuleResult {
    val ruleStatus = if (
      data.cas3Application?.submittedApplication?.latestBooking?.status == null &&
      data.cas3Application?.submittedApplication?.assessmentStatus == null &&
      (
        data.cas3Application?.applicationStatus == Cas3ApplicationStatus.REJECTED ||
          data.cas3Application?.applicationStatus == Cas3ApplicationStatus.IN_PROGRESS
        )
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
