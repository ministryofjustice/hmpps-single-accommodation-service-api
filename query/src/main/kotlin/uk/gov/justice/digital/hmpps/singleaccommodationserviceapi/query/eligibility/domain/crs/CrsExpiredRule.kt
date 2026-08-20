package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

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
class CrsExpiredRule(val clock: Clock) : Rule {
  override val description = "FAIL if CRS not within 12 weeks"
  override val reasonOnRuleFailure = FailureReason.CRS_EXPIRED
  lateinit var today: LocalDate
  fun isFail(data: DomainData) = !isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.sentAt?.toLocalDate(), today, 12L)

  override fun evaluate(data: DomainData): RuleResult {
    today = LocalDate.now(clock)
    val isFail = !isLessThanXWeeksInThePast(data.commissionedRehabilitativeServices?.sentAt?.toLocalDate(), today, 12L)
    return RuleResult(
      description = description,
      ruleStatus = if (isFail) RuleStatus.FAIL else RuleStatus.PASS,
      failureReason = if (isFail) reasonOnRuleFailure else null,
    )
  }
}

@Component
class CrsExpiredRuleMale(clock: Clock) : CrsExpiredRule(clock) {
  override val description = super.description + " and male"
  override val reasonOnRuleFailure = FailureReason.CRS_EXPIRED_MALE
  fun isMale(sexCode: SexCode?) = sexCode === SexCode.M
  override fun isFail(data: DomainData) = super.isFail(data) && isMale(data.sex)
}

@Component
class CrsExpiredRuleNonMale(clock: Clock) : CrsExpiredRule(clock) {
  override val description = super.description + " and non-male"
  override val reasonOnRuleFailure = FailureReason.CRS_EXPIRED_NON_MALE
  fun isMale(sexCode: SexCode?) = sexCode === SexCode.M
  override fun isFail(data: DomainData) = super.isFail(data) && !isMale(data.sex)
}
