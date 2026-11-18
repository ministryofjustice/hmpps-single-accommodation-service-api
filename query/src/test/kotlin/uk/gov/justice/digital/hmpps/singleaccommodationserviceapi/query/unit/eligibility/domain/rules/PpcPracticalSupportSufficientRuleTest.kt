package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcPracticalSupportSufficientRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.PublicProtectionCheckerAnswers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PpcPracticalSupportSufficientRuleTest {
  private val rule = PpcPracticalSupportSufficientRule()
  private val description = "FAIL if practical support sufficient"

  @ParameterizedTest
  @CsvSource("true, FAIL", "false, PASS", ", PASS")
  fun `rule fails only when practical support is sufficient`(answer: Boolean?, expectedStatus: RuleStatus) {
    val answers = answer?.let {
      PublicProtectionCheckerAnswers(
        supervisionRequired = false,
        controlMeasureSuitable = false,
        practicalSupportSufficient = it,
      )
    }

    assertThat(rule.evaluate(buildDomainData(ppcAnswers = answers))).isEqualTo(RuleResult(description, expectedStatus))
  }
}
