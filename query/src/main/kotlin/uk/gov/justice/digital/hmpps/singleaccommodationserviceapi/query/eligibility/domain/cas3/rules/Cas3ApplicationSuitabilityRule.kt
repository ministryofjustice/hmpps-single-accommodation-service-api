package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

private val suitableApplicationStatuses = listOf(
  Cas3ApplicationStatus.IN_PROGRESS,
  Cas3ApplicationStatus.AWAITING_PLACEMENT,
  Cas3ApplicationStatus.PLACED,
  Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
  Cas3ApplicationStatus.PENDING,
)

@Component
class Cas3ApplicationSuitabilityRule : Cas3SuitabilityRule {
  override val description = "FAIL if candidate does not have a suitable application"

  override fun evaluate(data: DomainData): RuleResult {
    val isSuitableApplication = data.cas3Application?.applicationStatus in suitableApplicationStatuses

    val ruleStatus = if (isSuitableApplication) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
