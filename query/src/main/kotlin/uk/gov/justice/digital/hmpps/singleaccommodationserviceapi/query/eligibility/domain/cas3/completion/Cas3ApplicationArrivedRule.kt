package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class Cas3ApplicationArrivedRule : Rule {
  override val description = "FAIL if CAS3 application is not arrived"

  override fun evaluate(data: DomainData): RuleResult {
    val isConfirmedBooking = data.cas3Application?.submittedApplication?.latestBooking?.status == Cas3BookingStatus.ARRIVED

    val ruleStatus = if (isConfirmedBooking) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
