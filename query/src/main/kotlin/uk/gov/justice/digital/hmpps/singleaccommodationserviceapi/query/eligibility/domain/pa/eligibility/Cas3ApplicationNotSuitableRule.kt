package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3ApplicationNotSuitableRule : PaEligibilityRule {
  override val description = "FAIL if candidate has suitable CAS3 application"

  override fun evaluate(data: DomainData): RuleResult {
    val suitableStatuses = listOf(
      Cas3ApplicationStatus.SUBMITTED,
      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
    )

    val isSuitableApplication = suitableStatuses.contains(data.cas3Application?.applicationStatus)

    val ruleStatus = if (isSuitableApplication) RuleStatus.FAIL else RuleStatus.PASS

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
