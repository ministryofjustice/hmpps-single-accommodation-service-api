package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2CompletionRuleSet::class,
    Cas2ApplicationCompletionRule::class,
    ClockConfig::class,
  ],
)
class Cas2CompletionRuleSetTest {

  @Autowired
  lateinit var cas2CompletionRuleSet: Cas2CompletionRuleSet

  private val expectedCas2CompletionRuleNames = listOf(
    Cas2ApplicationCompletionRule::class.simpleName,
  )

  @Test
  fun `all Cas2CompletionRule components are included in Cas2CompletionRuleSet`() {
    val ruleSetRules = cas2CompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas2CompletionRuleNames)
  }
}
