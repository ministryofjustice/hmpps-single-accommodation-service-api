package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrNotAcceptedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrNotAcceptedRuleTest {
  private val rule = DtrNotAcceptedRule()
  private val description = "FAIL if the DTR status is not 'Accepted'"

  @Test
  fun `accepted DTR passes`() {
    val result = rule.evaluate(
      buildDomainData(dutyToRefer = buildDutyToReferDto(status = DtrStatus.ACCEPTED)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = DtrStatus::class, names = ["ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `DTR that is not accepted fails`(status: DtrStatus) {
    val result = rule.evaluate(
      buildDomainData(dutyToRefer = buildDutyToReferDto(status = status)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing DTR fails`() {
    val result = rule.evaluate(buildDomainData(dutyToRefer = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
