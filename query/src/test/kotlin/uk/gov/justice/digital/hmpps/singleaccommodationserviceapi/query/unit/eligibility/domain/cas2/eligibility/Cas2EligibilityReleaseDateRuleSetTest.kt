package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityReleaseDateRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.InPrisonNoReleaseDateRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityReleaseDateRuleSet::class,
    InPrisonNoReleaseDateRule::class,
    ClockConfig::class,
  ],
)
class Cas2EligibilityReleaseDateRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityReleaseDateRuleSet: Cas2EligibilityReleaseDateRuleSet

  private val expectedRuleNames = listOf(
    InPrisonNoReleaseDateRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in Cas2EligibilityReleaseDateRuleSet`() {
    val ruleSetRules = cas2EligibilityReleaseDateRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
