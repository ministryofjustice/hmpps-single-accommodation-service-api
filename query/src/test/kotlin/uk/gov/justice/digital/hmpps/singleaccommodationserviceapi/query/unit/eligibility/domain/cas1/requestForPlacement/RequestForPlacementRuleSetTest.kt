package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.requestForPlacement

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.requestForPlacement.RequestForPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.requestForPlacement.rules.NoRequestForPlacementRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    RequestForPlacementRuleSet::class,
    NoRequestForPlacementRule::class,
    ClockConfig::class,
  ],
)
class RequestForPlacementRuleSetTest {

  @Autowired
  lateinit var ruleSet: RequestForPlacementRuleSet

  private val expectedRuleNames = listOf(
    NoRequestForPlacementRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
