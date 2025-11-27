package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.FemaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.ReferralTimingGuidanceRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.RulesEngine
import java.time.OffsetDateTime

class RulesEngineTest {
  private val sTierRule = STierRule()
  private val femaleRiskRule = FemaleRiskRule()
  private val referralTimingGuidanceRule = ReferralTimingGuidanceRule()
  val ruleSet = Cas1RuleSet()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )

  private val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()

  @Test
  fun `rules engine passes cas1 rules`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(listOf(), RuleSetStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails some cas1 rules`() {
    val data = DomainData(
      "C1S",
      sex = female,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      failedResults = listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(femaleRiskRule.description, RuleStatus.FAIL, false),
      ),
      ruleSetStatus = RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails just with a fail of guidance rule so should return GUIDANCE_FAIL`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      listOf(
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.FAIL, true),
      ),
      RuleSetStatus.GUIDANCE_FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails with a fail of guidance rule and a fail of non guidance rule so should return FAIL`() {
    val data = DomainData(
      tier = "A1S",
      sex = male,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = FinalResult(
      listOf(
        RuleResult(sTierRule.description, RuleStatus.FAIL, false),
        RuleResult(referralTimingGuidanceRule.description, RuleStatus.FAIL, true),
      ),
      RuleSetStatus.FAIL,
    )

    assertThat(result).isEqualTo(expectedResult)
  }
}
