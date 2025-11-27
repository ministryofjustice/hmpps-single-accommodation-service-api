package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.engine.DefaultRuleEvaluator

class RuleEvaluatorTest {
  @Test
  fun `default rule evaluator`() {
    val data = DomainData("A1")
    val rule = STierRule()
    val result = DefaultRuleEvaluator().evaluate(rule, data)

    val expectedResult = RuleResult(STierRule().description, RuleStatus.PASS)

    assertThat(result).isEqualTo(expectedResult)
  }

}