import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphEdge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphNodeKind
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphMarkdownRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphWalker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet

class RulesGraphTest {

  private val builder = DecisionTreeBuilder(RulesEngine(DefaultRuleSetEvaluator()))

  @Nested
  inner class WalkerTests {
    @Test
    fun `walk records PASS and FAIL edges and rules on RuleSet nodes`() {
      val pass = builder.confirmed()
      val fail = builder.notEligible(ServiceType.CAS1)
      val root = builder
        .ruleSet("ExampleEligibility", StubRuleSet(listOf(PrioritisedRule(StubRule("FAIL if example"))), "ExampleEligibility"))
        .onPass(pass)
        .onFail(fail).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)

      assertThat(graph.treeName).isEqualTo("EXAMPLE")
      assertThat(graph.nodes.map { it.title }).containsExactlyInAnyOrder(
        "ExampleEligibility",
        "return current status",
        "return status CAS1_NOT_ELIGIBLE",
      )
      assertThat(graph.edges.map { "${it.from}->${it.label}->${it.to}" }).containsExactlyInAnyOrder(
        "ExampleEligibility->PASS->return_current_status",
        "ExampleEligibility->FAIL->return_status_CAS1_NOT_ELIGIBLE",
      )
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }
      assertThat(ruleSetNode.rules).extracting<String> { it.className }.containsExactly("StubRule")
      assertThat(ruleSetNode.rules).extracting<String> { it.description }.containsExactly("FAIL if example")
    }

    @Test
    fun `walk uses the RuleSet name as the node title when it is set`() {
      val root = builder
        .ruleSet(
          "Cas1Completion",
          StubRuleSet(
            rules = listOf(PrioritisedRule(StubRule("FAIL if example"))),
            label = "Placement is upcoming",
          ),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }

      assertThat(ruleSetNode.title).isEqualTo("Placement is upcoming")
      assertThat(ruleSetNode.id).isEqualTo("Cas1Completion")
    }

    @Test
    fun `walk records source paths from each class package`() {
      val root = builder
        .ruleSet(
          "ExampleEligibility",
          StubRuleSet(listOf(PrioritisedRule(Cas1ApplicationPresentRule()), PrioritisedRule(CrsSubmittedRule()))),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1SuitabilityContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }

      assertThat(ruleSetNode.rules.map { it.sourcePath }).containsExactly(
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/cas1/suitability/Cas1ApplicationPresentRule.kt",
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/crs/completion/CrsSubmittedRule.kt",
      )
      assertThat(ruleSetNode.contextUpdater?.sourcePath).isEqualTo(
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/cas1/suitability/Cas1SuitabilityContextUpdater.kt",
      )
    }

    @Test
    fun `shared outcome is a single node with two incoming edges`() {
      val confirmed = builder.confirmed()
      val eligibility = builder
        .ruleSet("Eligibility", StubRuleSet(listOf(PrioritisedRule(StubRule("eligible")))))
        .onPass(confirmed)
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val upcoming = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(PrioritisedRule(StubRule("upcoming")))))
        .onPass(confirmed)
        .onFail(eligibility).applyContextUpdaterOnFailure(StubContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("SHARED", upcoming)

      assertThat(graph.nodes.filter { it.title == "return current status" }).hasSize(1)
      val confirmedId = graph.nodes.single { it.title == "return current status" }.id
      assertThat(graph.edges.filter { it.to == confirmedId && it.label == "PASS" }).hasSize(2)
    }

    @Test
    fun `continueWith emits PASS and FAIL to the same node`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(PrioritisedRule(StubRule("window"))), "Upcoming"))
        .continueWith(confirmed).propagateContextOnFailure()
        .build()

      val graph = RulesGraphWalker().walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val confirmedId = graph.nodes.single { it.title == "return current status" }.id
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
    fun `failure status is bound to the rule and context updater nodes are omitted`() {
      val confirmed = builder.confirmed()
      val upcoming = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(PrioritisedRule(StubRule("window"))), "Upcoming"))
        .continueWith(confirmed).setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED))
        .build()

      val graph = RulesGraphWalker().walk("DTR", upcoming)

      val upcomingId = graph.nodes.single { it.title == "Upcoming" }.id
      val confirmedId = graph.nodes.single { it.title == "return current status" }.id
      assertThat(graph.nodes.map { it.kind }).doesNotContain(GraphNodeKind.CONTEXT_UPDATER)
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
      assertThat(graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }.rules)
        .extracting<String> { it.failureStatus }
        .containsExactly("CAS1_NOT_STARTED")
    }

    @Test
    fun `a single failure status is bound to each rule in the ruleset`() {
      val root = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(PrioritisedRule(StubRule("window")))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1UpcomingContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("CAS1", root)

      assertThat(graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }.rules)
        .extracting<String> { it.failureStatus }
        .containsExactly("CAS1_UPCOMING")
    }

    @Test
    fun `status taken from a source application is labelled with that application`() {
      val root = builder
        .ruleSet("Suitability", StubRuleSet(listOf(PrioritisedRule(Cas1ApplicationPresentRule()))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1SuitabilityContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("CAS1", root)

      assertThat(graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }.rules)
        .extracting<String> { it.failureStatus }
        .containsExactly("Set to CAS1 Application Status")
    }
  }

  @Nested
  inner class RendererTests {
    @Test
    fun `render includes mermaid nodes, edges and rule catalogue`() {
      val root = builder
        .ruleSet("ExampleEligibility", StubRuleSet(listOf(PrioritisedRule(StubRule("FAIL if example")))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("## EXAMPLE")
      assertThat(markdown).contains("flowchart TD")
      assertThat(markdown).contains("ExampleEligibility(\"ExampleEligibility (1)")
      assertThat(markdown).contains("ExampleEligibility -->|PASS| return_current_status")
      assertThat(markdown).contains("ExampleEligibility -->|FAIL| return_status_CAS1_NOT_ELIGIBLE")
      assertThat(markdown).doesNotContain("_onFail")
      assertThat(markdown).doesNotContain("[ExampleEligibility](#EXAMPLE-ExampleEligibility)")
      assertThat(markdown).containsPattern("(?m)^.*`StubRule`.*: FAIL if example")
      assertThat(markdown).doesNotContain("- FAIL:")
      assertThat(markdown).contains("| StubRule | FAIL if example | ExampleEligibility | EXAMPLE |")
    }

    @Test
    fun `render uses the RuleSet name in the diagram, nodes and catalogue`() {
      val root = builder
        .ruleSet(
          "Cas1Completion",
          StubRuleSet(
            rules = listOf(PrioritisedRule(StubRule("FAIL if example"))),
            label = "Placement is upcoming",
          ),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("EXAMPLE", root)))

      assertThat(markdown).contains("Cas1Completion(\"Placement is upcoming (1)")
      assertThat(markdown).contains("**Placement is upcoming** (RuleSet)")
      assertThat(markdown).contains("| StubRule | FAIL if example | Placement is upcoming | EXAMPLE |")
      assertThat(markdown).doesNotContain("**Cas1Completion**")
    }

    @Test
    fun `render links to source files using each class package`() {
      val root = builder
        .ruleSet(
          "ExampleEligibility",
          StubRuleSet(listOf(PrioritisedRule(Cas1ApplicationPresentRule()), PrioritisedRule(CrsSubmittedRule()))),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1SuitabilityContextUpdater())
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("EXAMPLE", root)))

      assertThat(markdown).contains(
        "[`Cas1ApplicationPresentRule`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/cas1/suitability/Cas1ApplicationPresentRule.kt)",
      )
      assertThat(markdown).contains(
        "[`CrsSubmittedRule`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/crs/completion/CrsSubmittedRule.kt)",
      )
      assertThat(markdown).contains(
        "[`Cas1SuitabilityContextUpdater`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/cas1/suitability/Cas1SuitabilityContextUpdater.kt)",
      )
    }

    @Test
    fun `render lists named FAIL updater below the diagram and in the catalogue`() {
      val root = builder
        .ruleSet("Upcoming", StubRuleSet(listOf(PrioritisedRule(StubRule("window")))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(StubContextUpdater())
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("Upcoming -->|FAIL| return_status_CAS1_NOT_ELIGIBLE")
      assertThat(markdown).doesNotContain("Set_current_status_to_one_of_")
      assertThat(markdown).doesNotContain("(ContextUpdater)")
      assertThat(markdown).doesNotContain("[`StubContextUpdater`](#stubcontextupdater)")
      assertThat(markdown).contains("Used by: Upcoming (EXAMPLE)")
    }

    @Test
    fun `render describes constant FAIL updater without a mermaid node`() {
      val root = builder
        .ruleSet(
          "PaCompletion",
          StubRuleSet(listOf(PrioritisedRule(StubRule("complete"))), "PaCompletion"),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED))
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("PA", root)))

      assertThat(markdown).contains("PaCompletion -->|FAIL| return_status_CAS1_NOT_ELIGIBLE")
      assertThat(markdown).contains("- complete |CAS1_NOT_STARTED|")
      assertThat(markdown).doesNotContain("Set_CAS1_NOT_STARTED")
      assertThat(markdown).doesNotContain("(ContextUpdater)")
      assertThat(markdown).contains("- FAIL: Set CAS1_NOT_STARTED")
      assertThat(markdown).contains("Used by: PaCompletion (PA)")
      assertThat(markdown).contains("| CAS1_NOT_STARTED | START_APPROVED_PREMISE_APPLICATION (CAS1) | CAS1_START_APPLICATION |")
    }

    @Test
    fun `render appends the source application status to each rule`() {
      val root = builder
        .ruleSet(
          "Cas1Suitability",
          StubRuleSet(listOf(PrioritisedRule(Cas1ApplicationPresentRule()))),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1SuitabilityContextUpdater())
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("CAS1", root)))

      assertThat(markdown).contains(
        "- FAIL if candidate does not have an application |Set to CAS1 Application Status|",
      )
      assertThat(markdown).contains(
        "`Cas1ApplicationPresentRule`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/cas1/suitability/Cas1ApplicationPresentRule.kt): FAIL if candidate does not have an application |Set to CAS1 Application Status|",
      )
      assertThat(markdown).doesNotContain("(ContextUpdater)")
    }

    @Test
    fun `anonymous rules fail the walk`() {
      val anonymous = object : Rule {
        override val description = "anon"
        override fun evaluate(data: DomainData) = RuleResult(description, RuleStatus.PASS)
      }
      val root = builder
        .ruleSet("Named", StubRuleSet(listOf(PrioritisedRule(anonymous))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()

      assertThatThrownBy { RulesGraphWalker().walk("BROKEN", root) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("anonymous")
    }

    @Test
    fun `duplicate node names fail the walk`() {
      val confirmed = builder.confirmed()
      val notEligible = builder.notEligible(ServiceType.CAS1)
      val first = builder
        .ruleSet(
          "Same",
          StubRuleSet(
            listOf(
              PrioritisedRule(StubRule("first")),
            ),
          ),
        )
        .onPass(confirmed)
        .onFail(notEligible).propagateContextOnFailure()
        .build()
      val root = builder
        .ruleSet("Same", StubRuleSet(listOf(PrioritisedRule(StubRule("second")))))
        .onPass(first)
        .onFail(notEligible).propagateContextOnFailure()
        .build()

      assertThatThrownBy { RulesGraphWalker().walk("BROKEN", root) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Duplicate graph node id")
    }
  }
}
