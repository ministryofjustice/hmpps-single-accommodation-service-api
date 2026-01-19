package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.mockk
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNodeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock

class DecisionTreeBuilderTest {
  private val clock = MutableClock()
  private val engine: RulesEngine = mockk()

  @Test
  fun `ruleSet creates and returns RuleSetNodeBuilder`() {
    val ruleSet: RuleSet = mockk()
    val contextUpdater: ContextUpdater = mockk()
    val builder = DecisionTreeBuilder(engine)

    val result = builder.ruleSet("TestRuleSet", ruleSet, contextUpdater)

    assertThat(result).isInstanceOf(RuleSetNodeBuilder::class.java)
  }

  @Test
  fun `outcome creates OutcomeNode with fixed ServiceResult`() {
    val expectedResult =
      ServiceResult(
        serviceStatus = ServiceStatus.CONFIRMED,
        suitableApplicationId = UUID.randomUUID(),
      )
    val builder = DecisionTreeBuilder(engine)

    val result = builder.outcome(expectedResult)

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    // Verify it returns the fixed result
    val evalContext =
      EvalContext(
        data = buildDomainData(),
        current = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )
    val actualResult = result.eval(evalContext)
    assertThat(actualResult).isEqualTo(expectedResult)
  }

  @Test
  fun `confirmed creates OutcomeNode returning current context ServiceResult`() {
    val builder = DecisionTreeBuilder(engine)
    val currentResult =
      ServiceResult(
        serviceStatus = ServiceStatus.CONFIRMED,
        suitableApplicationId = UUID.randomUUID(),
      )
    val evalContext =
      EvalContext(
        data = buildDomainData(),
        current = currentResult,
      )

    val result = builder.confirmed()

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val actualResult = result.eval(evalContext)
    assertThat(actualResult).isEqualTo(currentResult)
  }

  @Test
  fun `confirmed returns different ServiceResult when context changes`() {
    val builder = DecisionTreeBuilder(engine)
    val initialResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    val updatedResult = ServiceResult(ServiceStatus.CONFIRMED)
    val initialContext =
      EvalContext(
        data = buildDomainData(),
        current = initialResult,
      )
    val updatedContext =
      EvalContext(
        data = buildDomainData(),
        current = updatedResult,
      )

    val confirmedNode = builder.confirmed()

    assertThat(confirmedNode.eval(initialContext)).isEqualTo(initialResult)
    assertThat(confirmedNode.eval(updatedContext)).isEqualTo(updatedResult)
  }

  @Test
  fun `notEligible creates OutcomeNode with NOT_ELIGIBLE status`() {
    val builder = DecisionTreeBuilder(engine)

    val result = builder.notEligible()

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val evalContext =
      EvalContext(
        data = buildDomainData(),
        current = ServiceResult(ServiceStatus.CONFIRMED),
      )
    val actualResult = result.eval(evalContext)
    assertThat(actualResult.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
    assertThat(actualResult.suitableApplicationId).isNull()
  }

  @Test
  fun `notEligible always returns same result regardless of context`() {
    val builder = DecisionTreeBuilder(engine)
    val context1 =
      EvalContext(
        data = buildDomainData(),
        current = ServiceResult(ServiceStatus.CONFIRMED),
      )
    val context2 =
      EvalContext(
        data = buildDomainData(),
        current = ServiceResult(ServiceStatus.SUBMITTED),
      )

    val notEligibleNode = builder.notEligible()

    val result1 = notEligibleNode.eval(context1)
    val result2 = notEligibleNode.eval(context2)

    assertThat(result1).isEqualTo(result2)
    assertThat(result1.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
  }
}