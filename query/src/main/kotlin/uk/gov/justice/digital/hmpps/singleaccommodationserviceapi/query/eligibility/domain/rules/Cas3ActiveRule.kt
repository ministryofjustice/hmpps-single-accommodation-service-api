package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas3ActiveRule : Rule<Cas3ApplicationData> {
  override val inputFields = setOf(DomainDataField.CAS3_APPLICATION_STATUS)
  override val description = "FAIL if CAS3 referral is submitted and active"
  override val failureReason = FailureReason.SUITABLE_CAS3_APPLICATION

  override fun evaluate(data: Cas3ApplicationData): RuleResult {
    val activeStatuses = listOf(
      Cas3ApplicationStatus.SUBMITTED,
      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
    )

    val isFail = activeStatuses.contains(data.cas3Application?.applicationStatus)

    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) failureReason else null,
    )
  }
}
