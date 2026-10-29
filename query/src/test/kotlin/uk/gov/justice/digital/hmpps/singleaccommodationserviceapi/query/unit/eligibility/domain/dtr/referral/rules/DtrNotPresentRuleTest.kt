package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.referral.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrNotPresentRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class)
  fun `person passes when DTR status is present`(dtrStaus: DtrStatus) {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = dtrStaus),
    )

    val result = DtrNotPresentRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person fails when DTR status is not present`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrNotPresentRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrNotPresentRule().description)
      .isEqualTo("FAIL if DTR status is not present")
  }
}
