package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcControlMeasureSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.PublicProtectionCheckerAnswers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PpcControlMeasureSuitableRuleTest {
  private val rule = PpcControlMeasureSuitableRule()
  private val description = "FAIL if control measure suitable"

  @ParameterizedTest
  @CsvSource("true, FAIL", "false, PASS", ", PASS")
  fun `rule fails only when control measure is suitable`(answer: Boolean?, expectedStatus: RuleStatus) {
    val answers = answer?.let {
      PublicProtectionCheckerAnswers(
        supervisionRequired = false,
        controlMeasureSuitable = it,
        practicalSupportSufficient = false,
      )
    }

    assertThat(rule.evaluate(buildDomainData(ppcAnswers = answers))).isEqualTo(RuleResult(description, expectedStatus))
  }
}
