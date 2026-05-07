package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    PaCompletionRuleSet::class,
    ClockConfig::class,
  ],
)
class PaCompletionRuleSetTest {

  @Autowired
  lateinit var paCompletionRuleSet: PaCompletionRuleSet

  private val expectedPaCompletionRuleNames = listOf<String>()

  @Test
  fun `all PaCompletionRule components are included in PaCompletionRuleSet`() {
    val ruleSetRules = paCompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(0)
      .containsExactlyInAnyOrderElementsOf(expectedPaCompletionRuleNames)
  }
}
