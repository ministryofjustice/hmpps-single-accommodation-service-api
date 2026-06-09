package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.prerequisite

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3PrerequisiteRuleSet::class,
    DtrExpiredReferralRule::class,
    CrsExpiredRule::class,
    CrsSubmittedRule::class,
    ClockConfig::class,
  ],
)
class Cas3PrerequisiteRuleSetTest {

  @Autowired
  lateinit var cas3PrerequisiteRuleSet: Cas3PrerequisiteRuleSet

  private val expectedCas3PrerequisiteRuleNames = listOf(
    DtrExpiredReferralRule::class.simpleName,
    CrsExpiredRule::class.simpleName,
    CrsSubmittedRule::class.simpleName,
  )

  @Test
  fun `all Cas3PrerequisiteRule components are included in Cas3PrerequisiteRuleSet`() {
    val ruleSetRules = cas3PrerequisiteRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedCas3PrerequisiteRuleNames)
  }
}
