package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class MaleRuleTest {
  private val description = "FAIL if person is Male"

  @Test
  fun `male person fails`() {
    val result = MaleRule().evaluate(buildDomainData(sex = SexCode.M))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @NullSource
  @EnumSource(value = SexCode::class, names = ["M"], mode = EnumSource.Mode.EXCLUDE)
  fun `person with non-male or missing sex passes`(sex: SexCode?) {
    val result = MaleRule().evaluate(buildDomainData(sex = sex))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
