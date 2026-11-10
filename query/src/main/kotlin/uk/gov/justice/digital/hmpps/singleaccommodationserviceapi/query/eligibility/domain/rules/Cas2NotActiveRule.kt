package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas2ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas2NotActiveRule : Rule<Cas2ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS2_ASSESSMENT_STATUS)
  override val description = "FAIL if assessment is present and inactive"

  override fun evaluate(data: Cas2ApplicationData): RuleResult {
    val unsuitableStatuses = listOf(
      Cas2AssessmentStatus.WITHDRAWN,
      Cas2AssessmentStatus.CANCELLED,
      Cas2AssessmentStatus.OFFER_DECLINED,
    )

    val isFail = unsuitableStatuses.contains(data.cas2Application?.submittedApplication?.latestAssessmentStatus)

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
