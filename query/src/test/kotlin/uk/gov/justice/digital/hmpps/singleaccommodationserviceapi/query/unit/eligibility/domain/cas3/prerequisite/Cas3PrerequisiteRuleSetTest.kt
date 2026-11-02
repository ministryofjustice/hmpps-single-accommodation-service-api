package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.prerequisite

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3PrerequisiteRuleSet::class,
    DtrExpiredRule::class,
    CrsSubmittedRuleMale::class,
    CrsSubmittedRuleNonMale::class,
    ClockConfig::class,
  ],
)
class Cas3PrerequisiteRuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas3PrerequisiteRuleSet

  private val expectedRuleNames = listOf(
    DtrExpiredRule::class.java.simpleName,
    CrsSubmittedRuleMale::class.java.simpleName,
    CrsSubmittedRuleNonMale::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
