import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphEdge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphNodeKind
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphMarkdownRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphWalker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult

class RulesGraphTest {

  private val builder = DecisionTreeBuilder(RulesEngine(DefaultRuleSetEvaluator()))

  @Nested
  inner class WalkerTests {
    @Test
    fun `walk records PASS and FAIL edges and rules on RuleSet nodes`() {
      val pass = builder.confirmed()
      val fail = builder.notEligible()
      val root = builder
        .ruleSet("ExampleEligibility", StubRuleSet(listOf(StubRule("FAIL if example"))))
        .onPass(pass)
        .onFail(fail)
        .build()

      val graph = RulesGraphWalker.walk("EXAMPLE", root)

      assertThat(graph.treeName).isEqualTo("EXAMPLE")
      assertThat(graph.cycles).isEmpty()
      assertThat(graph.nodes.map { it.title }).containsExactlyInAnyOrder(
        "ExampleEligibility",
        "confirmed",
        "notEligible",
      )
      assertThat(graph.edges.map { "${it.from}->${it.label}->${it.to}" }).containsExactlyInAnyOrder(
        "ExampleEligibility->PASS->confirmed",
        "ExampleEligibility->FAIL->notEligible",
      )
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }
      assertThat(ruleSetNode.rules).extracting<String> { it.className }.containsExactly("StubRule")
      assertThat(ruleSetNode.rules).extracting<String> { it.description }.containsExactly("FAIL if example")
    }

    @Test
    fun `shared outcome is a single node with two incoming edges`() {
      val confirmed = builder.confirmed()
      val eligibility = builder
        .ruleSet("Eligibility", StubRuleSet(listOf(StubRule("eligible"))))
        .onPass(confirmed)
        .onFail(builder.notEligible())
        .build()
      val upcoming = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(StubRule("upcoming"))), StubContextUpdater())
        .onPass(confirmed)
        .onFail(eligibility)
        .build()

      val graph = RulesGraphWalker.walk("SHARED", upcoming)

      assertThat(graph.nodes.filter { it.title == "confirmed" }).hasSize(1)
      val confirmedId = graph.nodes.single { it.title == "confirmed" }.id
      assertThat(graph.edges.filter { it.to == confirmedId && it.label == "PASS" }).hasSize(2)
      assertThat(graph.cycles).isEmpty()
    }

    @Test
    fun `continueWith emits PASS and FAIL to the same node`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(StubRule("window"))))
        .continueWith(confirmed)
        .build()

      val graph = RulesGraphWalker.walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val confirmedId = graph.nodes.single { it.title == "confirmed" }.id
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
  }

  @Nested
  inner class RendererTests {
    @Test
    fun `render includes mermaid nodes, edges and rule catalogue`() {
      val root = builder
        .ruleSet("ExampleEligibility", StubRuleSet(listOf(StubRule("FAIL if example"))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible())
        .build()
      val graph = RulesGraphWalker.walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("## EXAMPLE")
      assertThat(markdown).contains("flowchart TD")
      assertThat(markdown).contains("ExampleEligibility -->|PASS| confirmed")
      assertThat(markdown).contains("ExampleEligibility -->|FAIL| notEligible")
      assertThat(markdown).contains("`StubRule`: FAIL if example")
      assertThat(markdown).contains("| StubRule | FAIL if example | ExampleEligibility | EXAMPLE |")
    }
  }

  private class StubRule(override val description: String) : Rule {
    override fun evaluate(data: DomainData) = RuleResult(description, RuleStatus.PASS)
  }

  private class StubRuleSet(private val rules: List<Rule>) : RuleSet {
    override fun getRules(): List<Rule> = rules
  }

  private class StubContextUpdater : ContextUpdater() {
    override fun toServiceResult(context: EvaluationContext) = context.currentResult.copy(
      serviceStatus = ServiceStatus.UPCOMING,
    )
  }

  private class StubEligibilityTreeProvider(
    private val root: DecisionNode,
  ) : EligibilityTreeProvider {
    override fun tree() = root
    override fun initialContext(data: DomainData) = EvaluationContext(data, buildServiceResult())
  }
}
