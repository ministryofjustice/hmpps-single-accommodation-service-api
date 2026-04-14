package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrPresentRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class)
  fun `candidate passes when DTR status is present`(dtrStaus: DtrStatus) {
    val data = buildDomainData(
      dtrStatus = dtrStaus,
    )

    val result = DtrPresentRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR status is not present`() {
    val data = buildDomainData(
      dtrStatus = null,
    )

    val result = DtrPresentRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    Assertions.assertThat(DtrPresentRule().description)
      .isEqualTo("FAIL if DTR status is not present")
  }
}
