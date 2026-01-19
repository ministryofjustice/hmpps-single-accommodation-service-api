package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.OutcomeNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetNodeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock

class RuleSetNodeBuilderTest {
  private val clock = MutableClock()
  private val ruleSetName = "TestRuleSet"
  private val ruleSet: RuleSet = mockk()
  private val contextUpdater: ContextUpdater = mockk()
  private val engine: RulesEngine = mockk()

  @Test
  fun `onPass sets the onPass node and returns builder`() {
    val onPassNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.CONFIRMED)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
    val result = builder.onPass(onPassNode)

    assertThat(result).isSameAs(builder)
  }

  @Test
  fun `onFail sets the onFail node and returns builder`() {
    val onFailNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
    val result = builder.onFail(onFailNode)

    assertThat(result).isSameAs(builder)
  }

  @Test
  fun `builder can chain onPass and onFail`() {
    val onPassNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.CONFIRMED)
    }
    val onFailNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
    val chainedBuilder = builder.onPass(onPassNode).onFail(onFailNode)

    assertThat(chainedBuilder).isSameAs(builder)
  }

  @Test
  fun `build creates RuleSetNode with correct properties`() {
    val onPassNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.CONFIRMED)
    }
    val onFailNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
      .onPass(onPassNode)
      .onFail(onFailNode)

    val result = builder.build()

    assertThat(result).isInstanceOf(RuleSetNode::class.java)
    // Verify the node was created (we can't easily verify private fields, but we can verify it's the right type)
  }

  @Test
  fun `build throws exception when onPass is not set`() {
    val onFailNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
      .onFail(onFailNode)

    assertThatThrownBy { builder.build() }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `build throws exception when onFail is not set`() {
    val onPassNode: DecisionNode = OutcomeNode { _ ->
      ServiceResult(ServiceStatus.CONFIRMED)
    }

    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)
      .onPass(onPassNode)

    assertThatThrownBy { builder.build() }
      .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `build throws exception when neither onPass nor onFail are set`() {
    val builder = RuleSetNodeBuilder(ruleSetName, ruleSet, contextUpdater, engine)

    assertThatThrownBy { builder.build() }
      .isInstanceOf(IllegalArgumentException::class.java)
  }
}