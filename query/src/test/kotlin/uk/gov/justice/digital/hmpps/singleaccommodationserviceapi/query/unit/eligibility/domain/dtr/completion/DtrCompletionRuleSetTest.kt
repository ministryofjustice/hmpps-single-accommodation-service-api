package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrApplicationCompleteRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    DtrCompletionRuleSet::class,
    DtrApplicationCompleteRule::class,
  ],
)
class DtrCompletionRuleSetTest {

  @Autowired
  lateinit var dtrCompletionRuleSet: DtrCompletionRuleSet

  private val expectedDtrCompletionRuleNames = listOf(
    DtrApplicationCompleteRule::class.simpleName,
  )

  @Test
  fun `all DtrCompletionRule components are included in DtrCompletionRuleSet`() {
    val ruleSetRules = dtrCompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedDtrCompletionRuleNames)
  }
}
