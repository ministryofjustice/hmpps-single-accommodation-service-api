package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligible

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCommunityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CurrentAccommodationEligibleForCas2RuleSet::class,
    CurrentAccommodationTypeIsCas3Rule::class,
    CurrentAccommodationTypeIsCas2Rule::class,
    CurrentAccommodationTypeIsCommunityRule::class,
    ClockConfig::class,
  ],
)
class CurrentAccommodationEligibleForCas2RuleSetTest {

  @Autowired
  lateinit var ruleSet: CurrentAccommodationEligibleForCas2RuleSet

  private val expectedRuleNames = listOf(
    CurrentAccommodationTypeIsCas3Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCas2Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCommunityRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
