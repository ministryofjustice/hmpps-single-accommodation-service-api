package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1StatusRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.Cas1ApplicationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.Cas1PlacementRule

class Cas1StatusRuleSetTest {
  private val cas1StatusRuleSet = Cas1StatusRuleSet()
  private val cas1ApplicationRule = Cas1ApplicationRule()
  private val cas1PlacementRule = Cas1PlacementRule()

  @Test
  fun `check ruleset contains Cas1ApplicationRule`() {
    val result = cas1StatusRuleSet.getRules()

    assertThat(result.any { it is Cas1ApplicationRule }).isTrue()
    assertThat(result.find { it.description == cas1ApplicationRule.description }).isNotNull
  }

  @Test
  fun `check ruleset contains Cas1PlacementRule`() {
    val result = cas1StatusRuleSet.getRules()

    assertThat(result.any { it is Cas1PlacementRule }).isTrue()
    assertThat(result.find { it.description == cas1PlacementRule.description }).isNotNull
  }
}
