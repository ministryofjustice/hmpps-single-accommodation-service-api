package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.referral.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrWithdrawnRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class, names = ["WITHDRAWN"], mode = EnumSource.Mode.EXCLUDE)
  fun `person passes when DTR has not been withdrawn`(dtrStatus: DtrStatus) {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = dtrStatus),
    )

    val result = DtrWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person passes when DTR status is not present`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `person fails when DTR has been withdrawn`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = DtrStatus.WITHDRAWN),
    )

    val result = DtrWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrWithdrawnRule().description)
      .isEqualTo("FAIL if referral has been withdrawn")
  }
}
