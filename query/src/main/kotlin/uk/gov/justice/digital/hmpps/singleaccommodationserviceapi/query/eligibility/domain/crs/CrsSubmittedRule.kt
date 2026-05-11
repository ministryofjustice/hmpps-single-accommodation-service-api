package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import kotlin.collections.contains

@Component
class CrsSubmittedRule : Rule {
  override val description = "FAIL if CRS not submitted"

  override fun evaluate(data: DomainData): RuleResult {
    val status = data.commissionedRehabilitativeServices?.status
    return RuleResult(
      description = description,
      ruleStatus = if (status in SUBMITTED_STATUSES) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }

  companion object {
    private val SUBMITTED_STATUSES = setOf(
      CrsStatus.ACTION_PLAN_SUBMITTED,
      CrsStatus.ACTION_PLAN_APPROVED,
      CrsStatus.APPOINTMENT,
      CrsStatus.COMPLETED,
      CrsStatus.NSI_COMMENCED,
      CrsStatus.END_OF_SERVICE_REPORT,
    )
  }
}
