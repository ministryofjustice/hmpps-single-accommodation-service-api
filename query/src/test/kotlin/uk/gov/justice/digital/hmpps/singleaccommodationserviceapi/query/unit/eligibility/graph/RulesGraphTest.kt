package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoApplicationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsNotLiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphEdge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphNodeKind
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphHtmlRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphMarkdownRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphWalker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet

class RulesGraphTest {

  private val builder = DecisionTreeBuilder(RulesEngine(DefaultRuleSetEvaluator()))

  @Nested
  inner class WalkerTests {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `shared updater has one continuation per destination`(sameDestination: Boolean) {
      val updater = StubContextUpdater()
      val confirmed = builder.confirmed()
      val fail = if (sameDestination) confirmed else builder.notEligible(ServiceType.CAS1)
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("example"))))
        .onPass(confirmed).applyContextUpdaterOnPass(updater)
        .onFail(fail).applyContextUpdaterOnFailure(updater)
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val updaterNodes = graph.nodes.filter { it.kind == GraphNodeKind.CONTEXT_UPDATER }
      assertThat(updaterNodes).hasSize(if (sameDestination) 1 else 2)
      updaterNodes.forEach { node ->
        val outgoing = graph.edges.filter { it.from == node.id }
        assertThat(outgoing).hasSize(1)
        assertThat(outgoing.single().label).isEqualTo("CONTINUE")
      }
      assertThat(graph.edges.filter { it.to in updaterNodes.map { node -> node.id } }.map { it.label })
        .containsExactlyInAnyOrder("PASS", "FAIL")
      assertThat(graph.edges.filter { it.label == "CONTINUE" }.map { it.to })
        .containsExactlyInAnyOrderElementsOf(if (sameDestination) listOf("confirmed") else listOf("confirmed", "CAS1_NOT_ELIGIBLE"))

      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))
      val continuationIndexes = graph.edges.withIndex().filter { it.value.label == "CONTINUE" }.map { it.index }
      assertThat(markdown).contains("linkStyle ${continuationIndexes.joinToString(",")} stroke:#0088ff,stroke-width:2px")
      graph.edges.filter { it.label == "CONTINUE" }.forEach { edge ->
        assertThat(markdown).contains("${edge.from} -->|CONTINUE| ${edge.to}")
      }
      val html = RulesGraphHtmlRenderer.render(listOf(graph))
      assertThat(html).contains("edge[label = \"CONTINUE\"]")
      assertThat(html).contains("'line-color': '#0088ff', 'target-arrow-color': '#0088ff', 'color': '#0088ff'")
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `terminal context updater ends its branch as an outcome`(onPass: Boolean) {
      val updater = object : ContextUpdater() {
        override val terminalNode = true
        override val description = "Terminal updater"
        override fun toServiceResult(context: EvaluationContext) = ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED)
      }
      val hidden = builder.notEligible(ServiceType.CAS1)
      val confirmed = builder.confirmed()
      val rootBuilder = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("example"))))
      val root = if (onPass) {
        rootBuilder
          .onPass(hidden).applyContextUpdaterOnPass(updater)
          .onFail(confirmed).propagateContextOnFailure()
          .build()
      } else {
        rootBuilder
          .onPass(confirmed)
          .onFail(hidden).applyContextUpdaterOnFailure(updater)
          .build()
      }

      val graph = RulesGraphWalker().walk("EXAMPLE", root)

      val updaterNode = graph.nodes.single { it.title == updater.description }
      assertThat(updaterNode.kind).isEqualTo(GraphNodeKind.OUTCOME)

      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))
      assertThat(markdown).containsPattern("(?m)^ {2}class (.*,)?${updaterNode.id}(,.*)? outcomeNode$")
      assertThat(graph.nodes.map { it.title }).containsExactlyInAnyOrder(
        "ExampleEligibility",
        "Terminal updater",
        "current status",
      )
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge("ExampleEligibility", updaterNode.id, if (onPass) "PASS" else "FAIL"),
        GraphEdge("ExampleEligibility", "confirmed", if (onPass) "FAIL" else "PASS"),
      )
    }

    @Test
    fun `walk records PASS and FAIL edges and rules on RuleSet nodes`() {
      val pass = builder.confirmed()
      val fail = builder.notEligible(ServiceType.CAS1)
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("FAIL if example"))))
        .onPass(pass)
        .onFail(fail).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)

      assertThat(graph.treeName).isEqualTo("EXAMPLE")
      assertThat(graph.nodes.map { it.title }).containsExactlyInAnyOrder(
        "ExampleEligibility",
        "current status",
        "Not Eligible",
      )
      assertThat(graph.edges.map { "${it.from}->${it.label}->${it.to}" }).containsExactlyInAnyOrder(
        "ExampleEligibility->PASS->confirmed",
        "ExampleEligibility->FAIL->CAS1_NOT_ELIGIBLE",
      )
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }
      assertThat(ruleSetNode.rules).extracting<String> { it.className }.containsExactly("StubRule")
      assertThat(ruleSetNode.rules).extracting<String> { it.description }.containsExactly("FAIL if example")
    }

    @Test
    fun `walk records source paths from each class package`() {
      val root = builder
        .ruleSet(
          StubRuleSet(rules = listOf(Cas1NoApplicationRule(), CrsNotLiveRule())),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1StartContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }

      assertThat(ruleSetNode.rules.map { it.sourcePath }).containsExactly(
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/Cas1NoApplicationRule.kt",
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/CrsNotLiveRule.kt",
      )
      assertThat(ruleSetNode.onFailContextUpdater?.sourcePath).isEqualTo(
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/contextupdaters/Cas1StartContextUpdater.kt",
      )
    }

    @Test
    fun `shared outcome is a single node with two incoming edges`() {
      val confirmed = builder.confirmed()
      val eligibility = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("eligible")), name = "Eligibility"))
        .onPass(confirmed)
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val upcoming = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("upcoming")), name = "Upcoming"))
        .onPass(confirmed)
        .onFail(eligibility).applyContextUpdaterOnFailure(StubContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("SHARED", upcoming)

      assertThat(graph.nodes.filter { it.title == "current status" }).hasSize(1)
      val confirmedId = graph.nodes.single { it.title == "current status" }.id
      assertThat(graph.edges.filter { it.to == confirmedId && it.label == "PASS" }).hasSize(2)
    }

    @Test
    fun `continueWith emits PASS and FAIL to the same node`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("window")), name = "Upcoming"))
        .onPassAndFail(confirmed).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val confirmedId = graph.nodes.single { it.title == "current status" }.id
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge(
          upcomingId,
          confirmedId,
          "PASS",
        ),
        GraphEdge(
          upcomingId,
          confirmedId,
          "FAIL",
        ),
      )
    }

    @Test
    fun `add pass context updater if not identity`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet(
          StubRuleSet(
            rules =
            listOf(StubRule("window")),
            name = "Upcoming",
          ),
        )
        .applyContextUpdaterOnPass(ContextUpdater.constant(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED)))
        .onPassAndFail(confirmed).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val contextUpdaterId = graph.nodes.single { it.title == "Set current status to Not Started" }.id
      assertThat(graph.nodes.single { it.id == contextUpdaterId }.kind).isEqualTo(GraphNodeKind.CONTEXT_UPDATER)
      val confirmedId = graph.nodes.single { it.title == "current status" }.id
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge(
          upcomingId,
          contextUpdaterId,
          "PASS",
        ),
        GraphEdge(
          contextUpdaterId,
          confirmedId,
          "CONTINUE",
        ),
        GraphEdge(
          upcomingId,
          confirmedId,
          "FAIL",
        ),
      )
    }

    @Test
    fun `add fail context updater if not identity`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("window")), name = "Upcoming"))
        .onPassAndFail(confirmed)
        .setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED))
        .build()

      val graph = RulesGraphWalker().walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val contextUpdaterId = graph.nodes.single { it.title == "Set current status to Not Started" }.id
      assertThat(graph.nodes.single { it.id == contextUpdaterId }.kind).isEqualTo(GraphNodeKind.CONTEXT_UPDATER)
      val confirmedId = graph.nodes.single { it.title == "current status" }.id
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge(
          upcomingId,
          confirmedId,
          "PASS",
        ),
        GraphEdge(
          upcomingId,
          contextUpdaterId,
          "FAIL",
        ),
        GraphEdge(
          contextUpdaterId,
          confirmedId,
          "CONTINUE",
        ),
      )
    }
  }
}
