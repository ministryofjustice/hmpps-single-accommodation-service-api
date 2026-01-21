package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityRuleSet::class,
    Cas1CompletionRuleSet::class,
    STierEligibilityRule::class,
    ApplicationCompletionRule::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    ClockConfig::class,
  ],
)
class RulesEngineTest {
  private val defaultRulesEngine = RulesEngine(DefaultRuleSetEvaluator())
  private val crn = "ABC234"

  @Autowired
  private lateinit var cas1EligibilityRuleSet: Cas1EligibilityRuleSet

  @Test
  fun `rules engine passes cas1Eligibility rules`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(7),
    )

    val result = defaultRulesEngine.execute(cas1EligibilityRuleSet, data)

    assertThat(result).isEqualTo(RuleSetStatus.PASS)
  }

  @Test
  fun `rules engine fails some cas1Eligibility rules`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.C1S,
      sex = SexCode.F,
      releaseDate = LocalDate.now().plusMonths(7),
    )

    val result = defaultRulesEngine.execute(cas1EligibilityRuleSet, data)

    assertThat(result).isEqualTo(RuleSetStatus.FAIL)
  }
}
