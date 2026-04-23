package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class CrsSubmittedRule : CrsCompletionRule {
  override val description = "FAIL if CRS not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val submittedStatuses = listOf(
      CrsStatus.ACTION_PLAN_SUBMITTED,
      CrsStatus.ACTION_PLAN_APPROVED,
      CrsStatus.APPOINTMENT,
      CrsStatus.COMPLETED,
      CrsStatus.NSI_COMMENCED,
      CrsStatus.END_OF_SERVICE_REPORT,
    )

    return RuleResult(
      description = description,
      ruleStatus = if (submittedStatuses.contains(data.commissionedRehabilitativeServices?.status)) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
