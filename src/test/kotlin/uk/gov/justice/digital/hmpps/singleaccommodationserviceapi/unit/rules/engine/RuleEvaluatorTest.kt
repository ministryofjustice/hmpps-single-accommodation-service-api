package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator

class RuleEvaluatorTest {
  private val sTierRule = STierRule()
  private val maleRiskRule = MaleRiskRule()
  private val ruleEvaluator = DefaultRuleEvaluator()
  private val female = Sex(
    code = "F",
    description = "Female",
  )
  private val data = DomainData(
    tier = "A1",
    sex = female,
  )

  @Test
  fun `default rule evaluator passes sTierRule`() {
    val result = ruleEvaluator.evaluate(sTierRule, data)

    val expectedResult = RuleResult(sTierRule.description, RuleStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `default rule evaluator fails maleRiskRule`() {
    val result = ruleEvaluator.evaluate(maleRiskRule, data)

    val expectedResult = RuleResult(maleRiskRule.description, RuleStatus.FAIL)

    assertThat(result).isEqualTo(expectedResult)
  }
}
