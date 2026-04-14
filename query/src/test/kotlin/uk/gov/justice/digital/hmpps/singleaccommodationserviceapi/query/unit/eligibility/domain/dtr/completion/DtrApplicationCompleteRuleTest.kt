package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrApplicationCompleteRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrApplicationCompleteRuleTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["ACCEPTED"])
  fun `candidate fails when DTR status is not ACCEPTED`(dtrStaus: DtrStatus) {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = dtrStaus),
    )

    val result = DtrApplicationCompleteRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes when DTR status is ACCEPTED`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(status = DtrStatus.ACCEPTED),
    )

    val result = DtrApplicationCompleteRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR status is missing`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrApplicationCompleteRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrApplicationCompleteRule().description)
      .isEqualTo("FAIL if DTR not accepted")
  }
}
