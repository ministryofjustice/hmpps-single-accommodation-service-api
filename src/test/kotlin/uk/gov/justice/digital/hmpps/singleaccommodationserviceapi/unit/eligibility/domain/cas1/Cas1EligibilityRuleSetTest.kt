package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityBaseTest

class Cas1EligibilityRuleSetTest : EligibilityBaseTest() {
  @Test
  fun `check ruleset contains STierRule`() {
    val result = cas1RuleSet.getRules()

    assertThat(result.any { it is STierRule }).isTrue()
    assertThat(result.find { it.description == sTierRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains MaleRiskRule`() {
    val result = cas1RuleSet.getRules()

    assertThat(result.any { it is MaleRiskRule }).isTrue()
    assertThat(result.find { it.description == maleRiskRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains FemaleRiskRule`() {
    val result = cas1RuleSet.getRules()

    assertThat(result.any { it is NonMaleRiskRule }).isTrue()
    assertThat(result.find { it.description == nonMaleRiskRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains WithinSixMonthsOfReleaseRule`() {
    val result = cas1RuleSet.getRules()

    assertThat(result.any { it is WithinSixMonthsOfReleaseRule }).isTrue()
    assertThat(result.find { it.description == withinSixMonthsOfReleaseRule.description }).isNotNull
  }
}
