package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3CompletionRuleSet::class,
    Cas3ApplicationCompletionRule::class,
    ClockConfig::class,
  ],
)
class Cas3CompletionRuleSetTest {

  @Autowired
  lateinit var cas3CompletionRuleSet: Cas3CompletionRuleSet

  private val expectedCas3CompletionRuleNames = listOf(
    Cas3ApplicationCompletionRule::class.simpleName,
  )

  @Test
  fun `all Cas3CompletionRule components are included in Cas3CompletionRuleSet`() {
    val ruleSetRules = cas3CompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas3CompletionRuleNames)
  }
}
