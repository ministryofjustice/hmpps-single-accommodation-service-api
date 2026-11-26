package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.TreeExplanation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.loadScenarioChangeDates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.loadTreeExplanations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class RulesGraphTest {

  private val builder = DecisionTreeBuilder(RulesEngine(DefaultRuleSetEvaluator()))

  @Nested
  inner class TreeExplanationTests {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `uses the declared date for the matching tree and renders markdown safely`() {
      Files.writeString(
        directory.resolve("cas1-tree-explanation.md"),
        "# Latest\nDate: 2026-11-22\n\n| A | B |\n|---|---|\n| 1 | 2 |\n\n<script>alert(1)</script>",
      )
      Files.writeString(directory.resolve("cas2-tree-explanation.md"), "# CAS2\nDate: 2026-01-01")
      val root = builder.confirmed()
      val graphs = listOf("CAS1", "CAS2", "CAS3").map { RulesGraphWalker().walk(it, root) }

      val html = RulesGraphHtmlRenderer.render(graphs, loadTreeExplanations(directory))
      val json = html.substringAfter("<script type=\"application/json\" id=\"graph-data\">").substringBefore("</script>")
      val trees = JsonMapper.builder().build().readTree(json)["trees"]

      assertThat(trees[0]["explanation"]["date"].asText()).isEqualTo("2026-11-22")
      assertThat(trees[0]["explanation"]["html"].asText())
        .contains("<h1>Latest</h1>", "<table>", "&lt;script&gt;alert(1)&lt;/script&gt;")
        .doesNotContain("<script>")
      assertThat(trees[1]["explanation"]["html"].asText()).contains("<h1>CAS2</h1>")
      assertThat(trees[2]["explanation"].isNull).isTrue()
      assertThat(html).contains(
        "<div id=\"tree-explanation\"></div><div id=\"panel-content\"></div>",
        "No architecture decision record is available for this tree.",
        "treeExplanation.innerHTML = tree.explanation ?",
      )
    }

    @Test
    fun `rejects invalid Explanation dates and dated filenames`() {
      Files.writeString(directory.resolve("cas1-tree-explanation.md"), "Date: 2026-02-30")
      assertThatThrownBy { loadTreeExplanations(directory) }.isInstanceOf(IllegalArgumentException::class.java)

      Files.writeString(directory.resolve("cas1-tree-explanation.md"), "Date: 2026-01-01")
      Files.writeString(directory.resolve("cas2-tree-explanation-2026-01-02.md"), "Date: 2026-01-01")
      assertThatThrownBy { loadTreeExplanations(directory) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Invalid tree explanation filename")
    }

    @Test
    fun `compares the latest matching committed CSV change with each Explanation date`() {
      git("init", "--quiet")
      git("config", "user.name", "Graph test")
      git("config", "user.email", "graph-test@example.com")
      val resources = Files.createDirectories(directory.resolve("query/src/test/resources"))
      Files.writeString(resources.resolve("cas1-eligibility-scenarios.csv"), "original")
      Files.writeString(resources.resolve("cas2-eligibility-scenarios.csv"), "original")
      git("add", ".")
      commit("2026-01-01")
      Files.writeString(resources.resolve("cas1-application-scenarios.csv"), "application")
      git("add", ".")
      commit("2026-01-03")
      Files.writeString(resources.resolve("cas2-eligibility-scenarios.csv"), "uncommitted")

      val dates = loadScenarioChangeDates(directory, setOf("CAS1", "CAS2", "CAS3"))
      assertThat(dates).containsExactlyInAnyOrderEntriesOf(
        mapOf("CAS1" to LocalDate.parse("2026-01-03"), "CAS2" to LocalDate.parse("2026-01-01")),
      )

      val graphs = listOf("CAS1", "CAS2", "CAS3").map { RulesGraphWalker().walk(it, builder.confirmed()) }
      val explanations = mapOf(
        "CAS1" to TreeExplanation(LocalDate.parse("2026-01-02"), "# CAS1"),
        "CAS2" to TreeExplanation(LocalDate.parse("2026-01-01"), "# CAS2"),
      )
      val html = RulesGraphHtmlRenderer.render(graphs, explanations, dates)
      val json = html.substringAfter("<script type=\"application/json\" id=\"graph-data\">").substringBefore("</script>")
      val trees = JsonMapper.builder().build().readTree(json)["trees"]
      assertThat(trees[0]["explanation"]["scenarioChangeDate"].asText()).isEqualTo("2026-01-03")
      assertThat(trees[1]["explanation"]["scenarioChangeDate"].isNull).isTrue()
      assertThat(trees[2]["explanation"].isNull).isTrue()
      assertThat(html).contains("This Explanation may be out of date.", "after the Explanation date.")

      val newerExplanation = RulesGraphHtmlRenderer.render(graphs, mapOf("CAS1" to TreeExplanation(LocalDate.parse("2026-01-04"), "# CAS1")), dates)
      val newerJson = newerExplanation.substringAfter("<script type=\"application/json\" id=\"graph-data\">").substringBefore("</script>")
      assertThat(JsonMapper.builder().build().readTree(newerJson)["trees"][0]["explanation"]["scenarioChangeDate"].isNull).isTrue()
    }

    @Test
    fun `fetches history for shallow checkouts before comparing scenario dates`() {
      git("init", "--quiet")
      git("config", "user.name", "Graph test")
      git("config", "user.email", "graph-test@example.com")
      val resources = Files.createDirectories(directory.resolve("query/src/test/resources"))
      Files.writeString(resources.resolve("cas1-eligibility-scenarios.csv"), "original")
      git("add", ".")
      commit("2026-01-01")
      Files.writeString(directory.resolve("unrelated.txt"), "unrelated")
      git("add", ".")
      commit("2026-01-03")

      val clone = directory.resolve("shallow-clone")
      git("clone", "--quiet", "--depth=1", directory.toUri().toString(), clone.toString())
      assertThat(loadScenarioChangeDates(clone, setOf("CAS1")))
        .containsEntry("CAS1", LocalDate.parse("2026-01-01"))
    }

    private fun commit(date: String) {
      git("commit", "--quiet", "-m", "Scenario change", date = "${date}T12:00:00Z")
    }

    private fun git(vararg args: String, date: String? = null) {
      val command = ProcessBuilder(listOf("git", "-C", directory.toString()) + args).redirectErrorStream(true)
      if (date != null) {
        command.environment()["GIT_AUTHOR_DATE"] = date
        command.environment()["GIT_COMMITTER_DATE"] = date
      }
      val process = command.start()
      val output = process.inputStream.bufferedReader().use { it.readText() }
      check(process.waitFor() == 0) { "Git failed: $output" }
    }
  }

  @Nested
  inner class WalkerTests {
    @Test
    fun `both renderers label an empty ruleset with its name and explain why it passes`() {
      val ruleSet = StubRuleSet(rules = emptyList(), name = "Check eligibility")
      val root = builder.ruleSet(ruleSet)
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)

      assertThat(RulesGraphMarkdownRenderer.render(listOf(graph)))
        .contains("<div style='text-align:left'>Check eligibility\n(no rules - always pass)</div>")
      assertThat(RulesGraphHtmlRenderer.render(listOf(graph)))
        .contains("\"label\":\"Check eligibility\\n(no rules - always pass)\"")
    }

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
      assertThat(html)
        .contains("const currentPath = simulate().steps.some(step => step.node.id === target)")
        .contains("const allFailing = node.rules.every(rule => failing.has(rule.className))")
        .contains("const result = findRuleFailuresForNode(target)")
        .contains("scrollToSimulationRule(target);")
        .contains("renderSim('No consistent simulation path reaches this node.');\n          panel.scrollTop = 0;\n          return;")
        .doesNotContain("simTarget = target;")
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `terminal context updater ends its branch as an outcome`(onPass: Boolean) {
      val updater = object : ContextUpdater() {
        override val terminalNode = true
        override val description = "Terminal updater"
        override fun toServiceResult(context: EvaluationContext) = ServiceResultV2(serviceStatus = ServiceStatusV2.CAS1_NOT_STARTED)
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
        .applyContextUpdaterOnPass(ContextUpdater.constant(ServiceResultV2(serviceStatus = ServiceStatusV2.CAS1_NOT_STARTED)))
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
        .setConstantServiceResultOnFailure(ServiceResultV2(serviceStatus = ServiceStatusV2.CAS1_NOT_STARTED))
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
