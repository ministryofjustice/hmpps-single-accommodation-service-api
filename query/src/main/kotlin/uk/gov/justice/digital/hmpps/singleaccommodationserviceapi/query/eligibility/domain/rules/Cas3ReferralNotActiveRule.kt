package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas3ReferralNotActiveRule : Rule {
  override val description = "FAIL if the referral is rejected or in progress"

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
