package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligible

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.HasSexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.WrongTierRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    PersonEligibleRuleSet::class,
    Under18Rule::class,
    WrongTierRule::class,
    HasSexualOffenceRegistrationsRule::class,
    ClockConfig::class,
  ],
)
class PersonEligibleRuleSetTest {

  @Autowired
  lateinit var ruleSet: PersonEligibleRuleSet

  private val expectedRuleNames = listOf(
    Under18Rule::class.java.simpleName,
    WrongTierRule::class.java.simpleName,
    HasSexualOffenceRegistrationsRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
