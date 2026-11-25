package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPublicProtectionCheckerEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcControlMeasuresSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PpcControlMeasuresSuitableRuleTest {
  private val rule = PpcControlMeasuresSuitableRule()
  private val description = "FAIL if control measures suitable"

  @ParameterizedTest
  @CsvSource("true, FAIL", "false, PASS", ", PASS")
  fun `rule fails only when control measures is suitable`(answer: Boolean?, expectedStatus: RuleStatus) {
    val answers = answer?.let {
      buildPublicProtectionCheckerEntity(
        supervisionRequired = false,
        controlMeasuresSuitable = it,
        practicalSupportSufficient = false,
      )
    }

    assertThat(rule.evaluate(buildDomainData(publicProtectionChecker = answers))).isEqualTo(RuleResult(description, expectedStatus))
  }
}
