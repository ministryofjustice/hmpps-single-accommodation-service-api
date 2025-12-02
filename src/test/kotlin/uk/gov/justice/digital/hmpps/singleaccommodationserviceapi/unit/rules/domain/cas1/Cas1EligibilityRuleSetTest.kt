package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.FemaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.WithinSixMonthsOfReleaseRule

class Cas1EligibilityRuleSetTest {
  private val cas1EligibilityRuleSet = Cas1EligibilityRuleSet()
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val femaleRiskRule = FemaleRiskRule()
  private val withinSixMonthsOfReleaseRule = WithinSixMonthsOfReleaseRule()

  @Test
  fun `check ruleset contains STierRule`() {
    val result = cas1EligibilityRuleSet.getRules()

    assertThat(result.any { it is STierRule }).isTrue()
    assertThat(result.find { it.description == sTierRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains MaleRiskRule`() {
    val result = cas1EligibilityRuleSet.getRules()

    assertThat(result.any { it is MaleRiskRule }).isTrue()
    assertThat(result.find { it.description == maleRiskRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains FemaleRiskRule`() {
    val result = cas1EligibilityRuleSet.getRules()

    assertThat(result.any { it is FemaleRiskRule }).isTrue()
    assertThat(result.find { it.description == femaleRiskRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains WithinSixMonthsOfReleaseRule`() {
    val result = cas1EligibilityRuleSet.getRules()

    assertThat(result.any { it is WithinSixMonthsOfReleaseRule }).isTrue()
    assertThat(result.find { it.description == withinSixMonthsOfReleaseRule.description }).isNotNull
  }
}
