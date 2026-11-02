package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.eligible

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.NoActiveCas1OrCas3ReferralsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas1ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas3ActiveRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    NoActiveCas1OrCas3ReferralsRuleSet::class,
    Cas3ActiveRule::class,
    Cas1ActiveRule::class,
  ],
)
class NoActiveCas1OrCas3ReferralsRuleSetTest {

  @Autowired
  lateinit var ruleSet: NoActiveCas1OrCas3ReferralsRuleSet

  private val expectedRuleNames = listOf(
    Cas3ActiveRule::class.java.simpleName,
    Cas1ActiveRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
