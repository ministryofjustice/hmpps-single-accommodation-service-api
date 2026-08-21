package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isLessThanXWeeksInThePast
import java.time.Clock
import java.time.LocalDate

@Component
class CrsExpiredRuleNonMale(val clock: Clock) : Rule {
  override val description = "FAIL if CRS not within 12 weeks and non-male"
  override fun evaluate(data: DomainData): RuleResult {
    val today = LocalDate.now(clock)
    val isFail = !isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.sentAt?.toLocalDate(), today, 12L) && data.sex != SexCode.M
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) FailureReason.CRS_EXPIRED_NON_MALE else null,
    )
  }
}
