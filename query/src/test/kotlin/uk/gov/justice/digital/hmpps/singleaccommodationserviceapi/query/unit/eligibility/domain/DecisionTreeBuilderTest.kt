package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNodeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew

class DecisionTreeBuilderTest {
  private val engine: RulesEngine = mockk()

  @Test
  fun `ruleSet creates and returns RuleSetNodeBuilder`() {
    val ruleSet: RuleSet = mockk()
    val builder = DecisionTreeBuilder(engine)

    val result = builder.ruleSet("TestRuleSet", ruleSet)

    assertThat(result).isInstanceOf(RuleSetNodeBuilder::class.java)
  }

  @Test
  fun `named outcomes expose graph labels`() {
    val builder = DecisionTreeBuilder(engine)
    val expectedResult = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_BOOKED)

    assertThat(builder.confirmed().name).isEqualTo("return current status")
    assertThat(builder.notEligible(ServiceType.CAS1).name).isEqualTo("return status CAS1_NOT_ELIGIBLE")
    assertThat(builder.notRequired(ServiceType.DTR).name).isEqualTo("return status DTR_NOT_REQUIRED")
    assertThat(builder.outcome("placementBooked", expectedResult).name).isEqualTo("return status CAS1_PLACEMENT_BOOKED")
  }

  @Test
  fun `outcome creates OutcomeNode with fixed ServiceResult`() {
    val expectedResult =
      buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_BOOKED,
      )
    val builder = DecisionTreeBuilder(engine)

    val result = builder.outcome("placementBooked", expectedResult)

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    // Verify it returns the fixed result
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult).isEqualTo(expectedResult)
  }

  @Test
  fun `confirmed creates OutcomeNode returning current context ServiceResult`() {
    val builder = DecisionTreeBuilder(engine)
    val currentResult =
      buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_BOOKED,
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
    val initialResult = buildServiceResultNew()
    val updatedResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)
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
    val failureReasons = listOf(FailureReason.IS_SETTLED)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_NOT_ELIGIBLE, failureReasons = failureReasons),
    )

    val result = builder.confirmed().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatusNew.CAS1_NOT_ELIGIBLE)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }

  @Test
  fun `confirmed strips failureReasons when status is not NOT_ELIGIBLE`() {
    val builder = DecisionTreeBuilder(engine)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_NOT_STARTED, failureReasons = listOf(FailureReason.IS_SETTLED)),
    )

    val result = builder.confirmed().eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatusNew.CAS1_NOT_STARTED)
    assertThat(result.failureReasons).isEmpty()
  }

  @Test
  fun `notEligible creates OutcomeNode with NOT_ELIGIBLE status`() {
    val builder = DecisionTreeBuilder(engine)

    val result = builder.notEligible(ServiceType.CAS1)

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult.serviceStatus).isEqualTo(ServiceStatusNew.CAS1_NOT_ELIGIBLE)
  }

  @Test
  fun `notEligible always returns same result regardless of context`() {
    val builder = DecisionTreeBuilder(engine)
    val context1 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED),
      )
    val context2 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_SUBMITTED),
      )

    val notEligibleNode = builder.notEligible(ServiceType.CAS1)

    val result1 = notEligibleNode.eval(context1)
    val result2 = notEligibleNode.eval(context2)

    assertThat(result1).isEqualTo(result2)
    assertThat(result1.serviceStatus).isEqualTo(ServiceStatusNew.CAS1_NOT_ELIGIBLE)
  }

  @Test
  fun `notEligible carries failureReasons from current context`() {
    val builder = DecisionTreeBuilder(engine)
    val failureReasons = listOf(FailureReason.IS_SETTLED, FailureReason.MALE_NOT_HIGH_RISK_TIER)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED,
        failureReasons = failureReasons,
      ),
    )

    val result = builder.notEligible(ServiceType.CAS1).eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatusNew.CAS1_NOT_ELIGIBLE)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }

  @Test
  fun `notRequired creates OutcomeNode with NOT_REQUIRED status`() {
    val builder = DecisionTreeBuilder(engine)

    val result = builder.notRequired(ServiceType.DTR)

    assertThat(result).isInstanceOf(OutcomeNode::class.java)
    val evaluationContext =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED),
      )
    val actualResult = result.eval(evaluationContext)
    assertThat(actualResult.serviceStatus).isEqualTo(ServiceStatusNew.DTR_NOT_REQUIRED)
  }

  @Test
  fun `notRequired always returns same result regardless of context`() {
    val builder = DecisionTreeBuilder(engine)
    val context1 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED),
      )
    val context2 =
      EvaluationContext(
        data = buildDomainData(),
        currentResult = buildServiceResultNew(ServiceStatusNew.CAS1_SUBMITTED),
      )

    val notRequiredNode = builder.notRequired(ServiceType.DTR)

    val result1 = notRequiredNode.eval(context1)
    val result2 = notRequiredNode.eval(context2)

    assertThat(result1).isEqualTo(result2)
    assertThat(result1.serviceStatus).isEqualTo(ServiceStatusNew.DTR_NOT_REQUIRED)
  }

  @Test
  fun `notRequired carries failureReasons from current context`() {
    val builder = DecisionTreeBuilder(engine)
    val failureReasons = listOf(FailureReason.HAS_NEXT_ACCOMMODATION)
    val context = EvaluationContext(
      data = buildDomainData(),
      currentResult = buildServiceResultNew(
        serviceStatus = ServiceStatusNew.DTR_NOT_REQUIRED,
        failureReasons = failureReasons,
      ),
    )

    val result = builder.notRequired(ServiceType.DTR).eval(context)

    assertThat(result.serviceStatus).isEqualTo(ServiceStatusNew.DTR_NOT_REQUIRED)
    assertThat(result.failureReasons).isEqualTo(failureReasons)
  }
}
