package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1CompletionRuleSet::class,
    Cas1ApplicationCompletionRule::class,
    ClockConfig::class,
  ],
)
class Cas1CompletionRuleSetTest {

  @Autowired
  lateinit var cas1CompletionRuleSet: Cas1CompletionRuleSet

  private val expectedCas1CompletionRuleNames = listOf(
    Cas1ApplicationCompletionRule::class.simpleName,
  )

  @Test
  fun `all Cas1CompletionRule components are included in Cas1CompletionRuleSet`() {
    val ruleSetRules = cas1CompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas1CompletionRuleNames)
  }
}
