package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsSubmittedRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CrsCompletionRuleSet::class,
    CrsSubmittedRule::class,
    ClockConfig::class,
    CrsExpiredRule::class,
  ],
)
class CrsCompletionRuleSetTest {

  @Autowired
  lateinit var crsCompletionRuleSet: CrsCompletionRuleSet

  private val expectedCrsCompletionRuleNames = listOf(
    CrsSubmittedRule::class.simpleName,
    CrsExpiredRule::class.simpleName,
  )

  @Test
  fun `all CrsCompletionRule components are included in CrsCompletionRuleSet`() {
    val ruleSetRules = crsCompletionRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedCrsCompletionRuleNames)
  }
}
