package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1RuleSet::class,
    STierRule::class,
    MaleRiskRule::class,
    NonMaleRiskRule::class,
    WithinSixMonthsOfReleaseRule::class,
    ClockConfig::class,
  ],
)
class Cas1EligibilityRuleSetTest {

  @Autowired
  lateinit var cas1RuleSet: Cas1RuleSet

  private val expectedCas1RuleNames = listOf(
    MaleRiskRule::class.simpleName,
    NonMaleRiskRule::class.simpleName,
    STierRule::class.simpleName,
    WithinSixMonthsOfReleaseRule::class.simpleName,
  )

  @Test
  fun `all Cas1Rule components are included in Cas1RuleSet`() {
    val ruleSetRules = cas1RuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(4)
      .containsExactlyInAnyOrderElementsOf(expectedCas1RuleNames)
  }
}
