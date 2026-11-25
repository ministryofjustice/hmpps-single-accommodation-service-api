package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcAnswersNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.PublicProtectionCheckerAnswers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PpcAnswersNotPresentRuleTest {
  private val rule = PpcAnswersNotPresentRule()
  private val description = "FAIL if PPC answers have not been submitted"

  @Test
  fun `submitted answers pass even when all answers are no`() {
    val data = buildDomainData(
      ppcAnswers = PublicProtectionCheckerAnswers(
        supervisionRequired = false,
        controlMeasureSuitable = false,
        practicalSupportSufficient = false,
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing answers fail`() {
    val data = buildDomainData(ppcAnswers = null)

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
