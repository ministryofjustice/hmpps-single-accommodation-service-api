package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.submittedReferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral.Cas2SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral.rules.Cas2NotSubmittedRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2SubmittedRuleSet::class,
    Cas2NotSubmittedRule::class,
    ClockConfig::class,
  ],
)
class Cas2SubmittedRuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas2SubmittedRuleSet

  private val expectedRuleNames = listOf(
    Cas2NotSubmittedRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
