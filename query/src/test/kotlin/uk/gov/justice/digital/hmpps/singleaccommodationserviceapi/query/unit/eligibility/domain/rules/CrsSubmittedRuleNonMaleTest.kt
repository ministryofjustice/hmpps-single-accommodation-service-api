package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CrsSubmittedRuleNonMaleTest {
  private val rule = CrsSubmittedRuleNonMale()
  private val description = "FAIL if person is not male and the referral is neither live nor completed"

  @ParameterizedTest
  @NullSource
  @EnumSource(value = SexCode::class, names = ["M"], mode = EnumSource.Mode.EXCLUDE)
  fun `person who is not male passes with live or completed referral`(sex: SexCode?) {
    listOf(CrsReferralStatus.LIVE, CrsReferralStatus.COMPLETED).forEach { status ->
      val result = rule.evaluate(
        buildDomainData(
          sex = sex,
          commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(status = status),
        ),
      )

      assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
    }
  }

  @ParameterizedTest
  @NullSource
  @EnumSource(value = SexCode::class, names = ["M"], mode = EnumSource.Mode.EXCLUDE)
  fun `person who is not male fails with withdrawn or missing referral`(sex: SexCode?) {
    listOf(null, CrsReferralStatus.WITHDRAWN).forEach { status ->
      val result = rule.evaluate(
        buildDomainData(
          sex = sex,
          commissionedRehabilitativeServices = status?.let { buildCommissionedRehabilitativeServices(status = it) },
        ),
      )

      assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL, FailureReason.CRS_NOT_SUBMITTED_NON_MALE))
    }
  }

  @ParameterizedTest
  @NullSource
  @EnumSource(CrsReferralStatus::class)
  fun `male passes regardless of referral status`(status: CrsReferralStatus?) {
    val result = rule.evaluate(
      buildDomainData(
        sex = SexCode.M,
        commissionedRehabilitativeServices = status?.let { buildCommissionedRehabilitativeServices(status = it) },
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
