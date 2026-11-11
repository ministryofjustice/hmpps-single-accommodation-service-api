package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas2NotActiveRule : Rule {
  override val description = "FAIL if assessment is inactive"

  override fun evaluate(data: DomainData): RuleResult {
    val activeStatuses = listOf(
      Cas2AssessmentStatus.MORE_INFO_REQUESTED,
      Cas2AssessmentStatus.AWAITING_DECISION,
      Cas2AssessmentStatus.ON_WAITING_LIST,
      Cas2AssessmentStatus.PLACE_OFFERED,
      Cas2AssessmentStatus.OFFER_ACCEPTED,
      Cas2AssessmentStatus.AWAITING_ARRIVAL,
    )

    val isFail = data.cas2Application?.submittedApplication?.latestAssessmentStatus !in activeStatuses

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
