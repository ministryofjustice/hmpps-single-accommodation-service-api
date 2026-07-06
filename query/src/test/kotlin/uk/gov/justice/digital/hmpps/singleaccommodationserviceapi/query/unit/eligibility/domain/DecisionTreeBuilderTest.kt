package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNodeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult

class DecisionTreeBuilderTest {
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
      buildServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
      )
    val builder = DecisionTreeBuilder(engine)

    val result = builder.outcome(expectedResult)

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    // Verify it returns the fixed result
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult).isEqualTo(expectedResult)
  }

  @Test
  fun `confirmed creates OutcomeNode returning current context ServiceResult`() {
    val builder = DecisionTreeBuilder(engine)
    val currentResult =
      buildServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
      )
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = currentResult,
      )

    val result = builder.confirmed()

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult).isEqualTo(currentResult)
  }

  @Test
  fun `confirmed returns different ServiceResult when context changes`() {
    val builder = DecisionTreeBuilder(engine)
    val initialResult = buildServiceResult()
    val updatedResult = buildServiceResult(ServiceStatus.PLACEMENT_BOOKED)
    val initialContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = initialResult,
      )
    val updatedContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = updatedResult,
      )

    val confirmedNode = builder.confirmed()

    assertThat(confirmedNode.eval(initialContext)).isEqualTo(initialResult)
    assertThat(confirmedNode.eval(updatedContext)).isEqualTo(updatedResult)
  }

  @Test
  fun `confirmed preserves failureReasons when status is NOT_ELIGIBLE`() {
    val builder = DecisionTreeBuilder(engine)
    val failureReasons = listOf(FailureReason.INVALID_APPLICATION_STATE)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResult(ServiceStatus.NOT_ELIGIBLE, failureReasons = failureReasons),
    )

    val result = builder.confirmed().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }

  @Test
  fun `confirmed strips failureReasons when status is not NOT_ELIGIBLE`() {
    val builder = DecisionTreeBuilder(engine)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResult(ServiceStatus.NOT_STARTED, failureReasons = listOf(FailureReason.INVALID_APPLICATION_STATE)),
    )

    val result = builder.confirmed().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_STARTED)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `notEligible creates OutcomeNode with NOT_ELIGIBLE status`() {
    val builder = DecisionTreeBuilder(engine)

    val result = builder.notEligible()

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.PLACEMENT_BOOKED),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
  }

  @Test
  fun `notEligible always returns same result regardless of context`() {
    val builder = DecisionTreeBuilder(engine)
    val context1 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.PLACEMENT_BOOKED),
      )
    val context2 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.SUBMITTED),
      )

    val notEligibleNode = builder.notEligible()

    val result1 = notEligibleNode.eval(context1)
    val result2 = notEligibleNode.eval(context2)

    assertThat(result1).isEqualTo(result2)
    assertThat(result1.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
  }

  @Test
  fun `notEligible carries failureReasons from current context`() {
    val builder = DecisionTreeBuilder(engine)
    val failureReasons = listOf(FailureReason.S_TIER, FailureReason.SEX_DATA_NOT_AVAILABLE)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        failureReasons = failureReasons,
      ),
    )

    val result = builder.notEligible().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }

  @Test
  fun `notRequired creates OutcomeNode with NOT_REQUIRED status`() {
    val builder = DecisionTreeBuilder(engine)

    val result = builder.notRequired()

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.PLACEMENT_BOOKED),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult.serviceStatus).isEqualTo(ServiceStatus.NOT_REQUIRED)
  }

  @Test
  fun `notRequired always returns same result regardless of context`() {
    val builder = DecisionTreeBuilder(engine)
    val context1 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.PLACEMENT_BOOKED),
      )
    val context2 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResult(ServiceStatus.SUBMITTED),
      )

    val notRequiredNode = builder.notRequired()

    val result1 = notRequiredNode.eval(context1)
    val result2 = notRequiredNode.eval(context2)

    assertThat(result1).isEqualTo(result2)
    assertThat(result1.serviceStatus).isEqualTo(ServiceStatus.NOT_REQUIRED)
  }

  @Test
  fun `notRequired carries failureReasons from current context`() {
    val builder = DecisionTreeBuilder(engine)
    val failureReasons = listOf(FailureReason.S_TIER, FailureReason.SEX_DATA_NOT_AVAILABLE)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        failureReasons = failureReasons,
      ),
    )

    val result = builder.notRequired().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_REQUIRED)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }
}
