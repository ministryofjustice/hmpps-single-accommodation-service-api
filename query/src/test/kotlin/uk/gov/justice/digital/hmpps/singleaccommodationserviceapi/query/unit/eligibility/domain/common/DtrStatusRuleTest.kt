package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrStatusRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["NOT_STARTED"])
  fun `candidate passes when DTR status is started`(dtrStaus: DtrStatus) {
    val data = buildDomainData(
      dtrStatus = dtrStaus,
    )

    val result = DtrStatusRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR status is not started`() {
    val data = buildDomainData(
      dtrStatus = DtrStatus.NOT_STARTED,
    )

    val result = DtrStatusRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrStatusRule().description)
      .isEqualTo("FAIL if DTR status is not started")
  }
}
