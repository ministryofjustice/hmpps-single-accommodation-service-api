package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class DecisionNodeTest {

  @Nested
  inner class OutcomeNodeTests {
    @Test
    fun `OutcomeNode returns correct ServiceResult from outcome function`() {
      val expectedResult =
        ServiceResult(
          serviceStatus = ServiceStatus.CONFIRMED,
          suitableApplicationId = UUID.randomUUID(),
        )
      val outcomeNode = OutcomeNode { _ -> expectedResult }
      val context =
        EvaluationContext(
          data = buildDomainData(),
          currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
        )

      val result = outcomeNode.eval(context)

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `OutcomeNode handles context-based outcome function`() {
      val context =
        EvaluationContext(
          data = buildDomainData(),
          currentResult =
            ServiceResult(
              serviceStatus = ServiceStatus.CONFIRMED,
              suitableApplicationId = UUID.randomUUID(),
            ),
        )
      val outcomeNode = OutcomeNode { context -> context.currentResult }

      val result = outcomeNode.eval(context)

      assertThat(result).isEqualTo(context.currentResult)
    }

    @Test
    fun `OutcomeNode can transform context to different ServiceResult`() {
      val context =
        EvaluationContext(
          data = buildDomainData(),
          currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
        )
      val outcomeNode = OutcomeNode { context ->
        ServiceResult(
          serviceStatus = ServiceStatus.CONFIRMED,
          suitableApplicationId = context.data.cas1Application?.id,
        )
      }

      val result = outcomeNode.eval(context)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
      assertThat(result.suitableApplicationId).isEqualTo(context.data.cas1Application?.id)
    }
  }

  @Nested
  inner class RuleSetNodeTests {
    @Test
    fun `RuleSetNode on PASS executes ruleset and evaluates onPass node with unchanged context`() {
      val ruleSet: RuleSet = mockk()
      val engine: RulesEngine = mockk()
      val contextUpdater: ContextUpdater = mockk()
      val onPassNode: DecisionNode = mockk()
      val onFailNode: DecisionNode = mockk()

      val initialContext =
        EvaluationContext(
          data = buildDomainData(),
          currentResult = ServiceResult(ServiceStatus.CONFIRMED),
        )
      val expectedResult = ServiceResult(ServiceStatus.CONFIRMED)

      every { engine.execute(ruleSet, initialContext.data) } returns RuleSetStatus.PASS
      every { onPassNode.eval(initialContext) } returns expectedResult

      val ruleSetNode =
        RuleSetNode(
          ruleSet = ruleSet,
          engine = engine,
          onPass = onPassNode,
          onFail = onFailNode,
          contextUpdater = contextUpdater,
        )

      val result = ruleSetNode.eval(initialContext)

      assertThat(result).isEqualTo(expectedResult)
      verify { engine.execute(ruleSet, initialContext.data) }
      verify { onPassNode.eval(initialContext) }
      verify(exactly = 0) { contextUpdater.update(any()) }
      verify(exactly = 0) { onFailNode.eval(any()) }
    }

    @Test
    fun `RuleSetNode on FAIL executes ruleset, updates context, and evaluates onFail node`() {
      val ruleSet: RuleSet = mockk()
      val engine: RulesEngine = mockk()
      val contextUpdater: ContextUpdater = mockk()
      val onPassNode: DecisionNode = mockk()
      val onFailNode: DecisionNode = mockk()

      val initialContext =
        EvaluationContext(
          data = buildDomainData(),
          currentResult = ServiceResult(ServiceStatus.CONFIRMED),
        )
      val updatedContext =
        EvaluationContext(
          data = initialContext.data,
          currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
        )
      val expectedResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE)

      every { engine.execute(ruleSet, initialContext.data) } returns RuleSetStatus.FAIL
      every { contextUpdater.update(initialContext) } returns
        updatedContext
      every { onFailNode.eval(updatedContext) } returns expectedResult

      val ruleSetNode =
        RuleSetNode(
          ruleSet = ruleSet,
          engine = engine,
          onPass = onPassNode,
          onFail = onFailNode,
          contextUpdater = contextUpdater,
        )

      val result = ruleSetNode.eval(initialContext)

      assertThat(result).isEqualTo(expectedResult)
      verify { engine.execute(ruleSet, initialContext.data) }
      verify { contextUpdater.update(initialContext) }
      verify { onFailNode.eval(updatedContext) }
      verify(exactly = 0) { onPassNode.eval(any()) }
    }

    @Test
    fun `RuleSetNode passes correct parameters to RulesEngine execute`() {
      val ruleSet: RuleSet = mockk()
      val engine: RulesEngine = mockk()
      val contextUpdater: ContextUpdater = mockk()
      val onPassNode: DecisionNode = mockk()
      val onFailNode: DecisionNode = mockk()

      val domainData = buildDomainData()
      val context =
        EvaluationContext(
          data = domainData,
          currentResult = ServiceResult(ServiceStatus.CONFIRMED),
        )

      every { engine.execute(any(), any()) } returns RuleSetStatus.PASS
      every { onPassNode.eval(any()) } returns ServiceResult(ServiceStatus.CONFIRMED)

      val ruleSetNode =
        RuleSetNode(
          ruleSet = ruleSet,
          engine = engine,
          onPass = onPassNode,
          onFail = onFailNode,
          contextUpdater = contextUpdater,
        )

      ruleSetNode.eval(context)

      verify { engine.execute(ruleSet, domainData) }
    }

    @Test
    fun `RuleSetNode passes correct parameters to ContextUpdater on FAIL`() {
      val ruleSet: RuleSet = mockk()
      val engine: RulesEngine = mockk()
      val contextUpdater: ContextUpdater = mockk()
      val onPassNode: DecisionNode = mockk()
      val onFailNode: DecisionNode = mockk()

      val context =
        EvaluationContext(
          data = buildDomainData(),
          currentResult = ServiceResult(ServiceStatus.CONFIRMED),
        )
      val updatedContext = EvaluationContext(
        data = context.data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      every { engine.execute(any(), any()) } returns RuleSetStatus.FAIL
      every { contextUpdater.update(context) } returns updatedContext
      every { onFailNode.eval(any()) } returns ServiceResult(ServiceStatus.NOT_ELIGIBLE)

      val ruleSetNode =
        RuleSetNode(
          ruleSet = ruleSet,
          engine = engine,
          onPass = onPassNode,
          onFail = onFailNode,
          contextUpdater = contextUpdater,
        )

      ruleSetNode.eval(context)

      verify { contextUpdater.update(context) }
    }
  }
}