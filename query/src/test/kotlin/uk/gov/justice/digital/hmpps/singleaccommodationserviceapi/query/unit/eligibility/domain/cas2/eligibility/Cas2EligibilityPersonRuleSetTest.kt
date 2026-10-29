package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityPersonRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.SexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.SingleLetterTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Under18Rule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityPersonRuleSet::class,
    Under18Rule::class,
    SingleLetterTierRule::class,
    SexualOffenceRegistrationsRule::class,
    ClockConfig::class,
  ],
)
class Cas2EligibilityPersonRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityPersonRuleSet: Cas2EligibilityPersonRuleSet

  private val expectedRuleNames = listOf(
    Under18Rule::class.java.simpleName,
    SingleLetterTierRule::class.java.simpleName,
    SexualOffenceRegistrationsRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in Cas2EligibilityPersonRuleSet`() {
    val ruleSetRules = cas2EligibilityPersonRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
