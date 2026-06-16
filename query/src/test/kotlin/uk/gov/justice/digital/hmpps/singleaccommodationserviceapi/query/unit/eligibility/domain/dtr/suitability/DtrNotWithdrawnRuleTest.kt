package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrNotWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrNotWithdrawnRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class, names = ["WITHDRAWN"], mode = EnumSource.Mode.EXCLUDE)
  fun `candidate passes when DTR has not been withdrawn`(dtrStatus: DtrStatus) {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = dtrStatus),
    )

    val result = DtrNotWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when DTR status is not present`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrNotWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR has been withdrawn`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = DtrStatus.WITHDRAWN),
    )

    val result = DtrNotWithdrawnRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrNotWithdrawnRule().description)
      .isEqualTo("FAIL if DTR referral has been withdrawn")
  }
}
