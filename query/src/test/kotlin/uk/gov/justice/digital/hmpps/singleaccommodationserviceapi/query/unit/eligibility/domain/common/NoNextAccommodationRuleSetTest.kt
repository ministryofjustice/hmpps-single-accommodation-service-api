package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.HasNextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    NoNextAccommodationRuleSet::class,
    HasNextAccommodationRule::class,
    ClockConfig::class,
  ],
)
class NoNextAccommodationRuleSetTest {

  @Autowired
  lateinit var ruleSet: NoNextAccommodationRuleSet

  private val expectedRuleNames = listOf(
    HasNextAccommodationRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    Assertions.assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
