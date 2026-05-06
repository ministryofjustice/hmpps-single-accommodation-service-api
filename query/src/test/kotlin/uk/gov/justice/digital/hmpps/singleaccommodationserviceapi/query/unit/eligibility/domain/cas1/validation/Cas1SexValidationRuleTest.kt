package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1SexValidationRuleTest {
  private val description = "FAIL if candidate has no sex"

  @Test
  fun `candidate passes if sex is present`() {
    val data = buildDomainData(
      sex = SexCode.M,
    )

    val result = Cas1SexValidationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails if sex data is missing`() {
    val data = buildDomainData(
      sex = null,
    )

    val result = Cas1SexValidationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.SEX_DATA_NOT_AVAILABLE))
  }

  @Test
  fun `rule has correct description`() {
    val result = Cas1SexValidationRule().description
    assertThat(result).isEqualTo("FAIL if candidate has no sex")
  }
}
