package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.FemaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.ReferralTimingGuidanceRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule

class Cas1RuleSetTest {
  private val cas1RuleSet = Cas1RuleSet()
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val femaleRiskRule = FemaleRiskRule()
  private val referralTimingGuidanceRule = ReferralTimingGuidanceRule()

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

    assertThat(result.any { it is FemaleRiskRule }).isTrue()
    assertThat(result.find { it.description == femaleRiskRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains ReferralTimingGuidanceRule`() {
    val result = cas1RuleSet.getRules()

    assertThat(result.any { it is ReferralTimingGuidanceRule }).isTrue()
    assertThat(result.find { it.description == referralTimingGuidanceRule.description }).isNotNull
  }
}
