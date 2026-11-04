package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus

@Component
class Cas1NotActiveRule : Rule {
  override val description = "FAIL if the referral is inactive or unsubmitted"

  override fun evaluate(data: DomainData): RuleResult {
    val unsuitableStatuses = listOf(
      Cas1ApplicationStatus.STARTED,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.EXPIRED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.WITHDRAWN,
    )

    val isFail = unsuitableStatuses.contains(data.cas1Application?.application?.status) && data.cas1Application?.requestForPlacement?.status == null && data.cas1Application?.placement?.status == null

    val ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
