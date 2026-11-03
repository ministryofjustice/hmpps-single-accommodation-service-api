package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.arrived

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.Cas2ArrivedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotMarkedAsArrivedRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2ArrivedRuleSet::class,
    Cas2NotArrivedRule::class,
    Cas2NotMarkedAsArrivedRule::class,
    ClockConfig::class,
  ],
)
class Cas2ArrivedRuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas2ArrivedRuleSet

  private val expectedRuleNames = listOf(
    Cas2NotArrivedRule::class.java.simpleName,
    Cas2NotMarkedAsArrivedRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
