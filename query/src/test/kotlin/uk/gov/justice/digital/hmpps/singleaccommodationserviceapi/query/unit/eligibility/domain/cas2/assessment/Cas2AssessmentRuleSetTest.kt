package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.assessment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.Cas2AssessmentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.rules.Cas2NotActiveRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2AssessmentRuleSet::class,
    Cas2NotActiveRule::class,
    ClockConfig::class,
  ],
)
class Cas2AssessmentRuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas2AssessmentRuleSet

  private val expectedRuleNames = listOf(
    Cas2NotActiveRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
