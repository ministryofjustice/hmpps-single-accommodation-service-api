package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1CompletionRuleSet::class,
    ApplicationCompletionRule::class,
    ClockConfig::class,
  ],
)
class Cas1CompletionRuleSetTest {

  @Autowired
  lateinit var cas1CompletionRuleSet: Cas1CompletionRuleSet

  private val expectedCas1CompletionRuleNames = listOf(
    ApplicationCompletionRule::class.simpleName,
  )

  @Test
  fun `all Cas1CompletionRule components are included in Cas1CompletionRuleSet`() {
    val ruleSetRules = cas1CompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas1CompletionRuleNames)
  }
}
