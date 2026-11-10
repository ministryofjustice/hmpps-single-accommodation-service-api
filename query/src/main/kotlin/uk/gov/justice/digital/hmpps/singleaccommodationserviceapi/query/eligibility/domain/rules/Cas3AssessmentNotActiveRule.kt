package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas3AssessmentNotActiveRule : Rule<Cas3ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS3_ASSESSMENT_STATUS, DomainDataField.CAS3_BOOKING_STATUS)
  override val description = "FAIL if the assessment is closed or rejected"

  override fun evaluate(data: Cas3ApplicationData): RuleResult {
    val submittedApplication = data.cas3Application?.submittedApplication
    val ruleStatus = if (
      submittedApplication?.latestBooking?.status == null &&
      FAILING_ASSESSMENT_STATUSES.contains(submittedApplication?.assessmentStatus)
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

  companion object {
    private val FAILING_ASSESSMENT_STATUSES = setOf(
      Cas3AssessmentStatus.CLOSED,
      Cas3AssessmentStatus.REJECTED,
    )
  }
}
