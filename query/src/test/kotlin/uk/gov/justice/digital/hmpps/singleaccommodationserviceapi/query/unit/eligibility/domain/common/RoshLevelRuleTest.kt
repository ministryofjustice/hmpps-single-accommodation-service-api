package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshLevelRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class RoshLevelRuleTest {
  private val rule = RoshLevelRule()
  private val description = "FAIL if person has no ROSH level"

  @Test
  fun `person passes when they have a ROSH level`() {
    val data = buildDomainData(roshLevel = buildRoshLevel())

    assertThat(rule.evaluate(data))
      .isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person fails when they have no ROSH level`() {
    val data = buildDomainData(roshLevel = null)

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.HAS_NO_ROSH_LEVEL,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
