package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas3NotRejectedRule : Rule {
  override val description = "FAIL unless there is no booking and either the assessment is rejected, or there is no assessment and the application is rejected"

  override fun evaluate(data: DomainData): RuleResult {
    val application = data.cas3Application
    val submittedApplication = application?.submittedApplication
    val isRejected = submittedApplication?.latestBooking?.status == null &&
      (
        submittedApplication?.assessmentStatus == Cas3AssessmentStatus.REJECTED ||
          (
            submittedApplication?.assessmentStatus == null &&
              application?.applicationStatus == Cas3ApplicationStatus.REJECTED
            )
        )
    val ruleStatus = if (isRejected) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
