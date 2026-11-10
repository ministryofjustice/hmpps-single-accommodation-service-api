package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoLiveCrsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.DomainDataFieldCatalog
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphEdge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GraphNodeKind
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphHtmlRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphMarkdownRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphWalker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.StubRuleSet

class RulesGraphTest {

  private val builder = DecisionTreeBuilder<DomainData>(RulesEngine(DefaultRuleSetEvaluator()))

  @Nested
  inner class WalkerTests {
    @Test
    fun `adding and removing a rule updates the inputs without a service catalogue change`() {
      val rules = mutableListOf<Rule<DomainData>>(MaleRiskEligibilityRule())
      val root = builder
        .ruleSet(StubRuleSet(rules))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()

      fun inputs() = RulesGraphWalker().walk("NEW_SERVICE", root).inputFields

      assertThat(inputs()).containsExactly(DomainDataField.TIER_SCORE)

      val crsRule = NoLiveCrsRule()
      rules += crsRule
      assertThat(inputs()).containsExactlyInAnyOrder(DomainDataField.TIER_SCORE, DomainDataField.CRS_STATUS)

      rules -= crsRule
      assertThat(inputs()).containsExactly(DomainDataField.TIER_SCORE)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `nested rulesets and updaters contribute deduplicated inputs on either branch`(onPass: Boolean) {
      val confirmed = builder.confirmed()
      val nested = builder
        .ruleSet(StubRuleSet(listOf(MaleRiskEligibilityRule(), NoLiveCrsRule()), "Nested"))
        .onPass(confirmed)
        .onFail(confirmed)
        .build()
      val updater = object : ContextUpdater<DomainData>() {
        override val inputFields = setOf(DomainDataField.SEX, DomainDataField.TIER_SCORE)
        override val description = "Choose by sex"
        override fun toServiceResult(context: EvaluationContext<DomainData>) = ServiceResultNew(ServiceStatusNew.CRS_SUBMITTED)
      }
      val rootBuilder = builder.ruleSet(StubRuleSet(listOf(MaleRiskEligibilityRule())))
      val root = if (onPass) {
        rootBuilder.onPass(nested).applyContextUpdaterOnPass(updater).onFail(confirmed).build()
      } else {
        rootBuilder.onPass(confirmed).onFail(nested).applyContextUpdaterOnFailure(updater).build()
      }

      val graph = RulesGraphWalker().walk("NEW_SERVICE", root)
      assertThat(graph.inputFields).containsExactlyInAnyOrder(
        DomainDataField.TIER_SCORE,
        DomainDataField.CRS_STATUS,
        DomainDataField.SEX,
      )
      assertThat(DomainDataFieldCatalog.fieldsFor(graph.inputFields).map { it.path })
        .containsExactly("sex", "tierScore", "commissionedRehabilitativeServices.status")
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
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `terminal context updater ends its branch as an outcome`(onPass: Boolean) {
      val updater = object : ContextUpdater<DomainData>() {
        override val inputFields = setOf(DomainDataField.SEX)
        override val terminalNode = true
        override val description = "Terminal updater"
        override fun toServiceResult(context: EvaluationContext<DomainData>) = ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED)
      }
      val hidden = builder
        .ruleSet(StubRuleSet(listOf(MaleRiskEligibilityRule()), "Hidden"))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()
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

      assertThat(graph.inputFields).containsExactly(DomainDataField.SEX)
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
          StubRuleSet(rules = listOf(Cas1NoReferralRule(), NoLiveCrsRule())),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1StartContextUpdater())
        .build()

      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }

      assertThat(ruleSetNode.rules.map { it.sourcePath }).containsExactly(
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/Cas1NoReferralRule.kt",
        "../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/NoLiveCrsRule.kt",
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

  @Nested
  inner class
  RendererTests {
    @Test
    fun `every declared field has a catalogue entry`() {
      val fields = DomainDataFieldCatalog.fieldsFor(DomainDataField.entries.toSet())

      assertThat(fields.map { it.path }).containsExactlyElementsOf(DomainDataField.entries.map { it.path })
      assertThat(fields).allSatisfy {
        assertThat(it.label).isNotBlank()
        assertThat(it.options).isNotEmpty()
      }
    }

    @Test
    fun `accommodation options list every reference data type`() {
      val field = DomainDataFieldCatalog.fieldsFor(setOf(DomainDataField.CURRENT_ACCOMMODATION_TYPE)).single()

      assertThat(field.options.first()).isEqualTo("Not available")
      assertThat(field.options.drop(1).map { it.substringBefore(" — ") }).containsExactly(
        "A01A", "A01C", "A01D", "A02", "A03", "A04", "A07A", "A07B", "A08", "A08A",
        "A08C", "A10", "A11", "A12", "A13", "A14", "A15", "A16", "A17", "HMP",
      )
    }

    @Test
    fun `next accommodation options use confirmed label`() {
      val field = DomainDataFieldCatalog.fieldsFor(setOf(DomainDataField.NEXT_ACCOMMODATIONS)).single()

      assertThat(field.options).containsExactly("None", "Confirmed")
    }

    @Test
    fun `nested inputs include their nullable parent availability rows once`() {
      val fields = DomainDataFieldCatalog.fieldsFor(
        setOf(DomainDataField.CAS3_BOOKING_STATUS, DomainDataField.CAS3_APPLICATION),
      )

      assertThat(fields.map { it.path }).containsExactly(
        "cas3Application",
        "cas3Application.submittedApplication.latestBooking",
        "cas3Application.submittedApplication.latestBooking.status",
      )
      assertThat(DomainDataFieldCatalog.fieldsFor(emptySet())).isEmpty()
    }

    @Test
    fun `catalogue uses existing enum values and descriptions`() {
      val fields = DomainDataFieldCatalog.fieldsFor(DomainDataField.entries.toSet())
      val accommodationOptions = fields.single { it.label == "Current accommodation type" }.options
      AddressUsageCode.entries.filter { it.name.matches(Regex("A\\d+.*")) }.forEach {
        assertThat(accommodationOptions).contains("${it.name} — ${it.description}")
      }
      val sexOptions = fields.single { it.path == "sex" }.options
      assertThat(sexOptions.map { it.substringBefore(" — ") })
        .containsExactlyInAnyOrderElementsOf(listOf("Not available") + SexCode.entries.map { it.name })
      assertThat(sexOptions).containsExactlyElementsOf(
        listOf("Not available") + SexCode.entries.map { "${it.name} — ${it.gender}" },
      )
      assertThat(fields.single { it.path == "roshLevel" }.options)
        .containsExactly("Not available", "Low", "Medium", "High", "Very high")
    }

    @Test
    fun `HTML lists domain data fields and options for the selected service`() {
      val inputs = setOf(
        DomainDataField.CAS2_ASSESSMENT_STATUS,
        DomainDataField.TIER_SCORE,
        DomainDataField.DATE_OF_BIRTH,
        DomainDataField.SEXUAL_OFFENCE_REGISTRATIONS,
        DomainDataField.CURRENT_ACCOMMODATION_END_DATE,
      )
      val fields = DomainDataFieldCatalog.fieldsFor(inputs)
      val assessmentStatus = fields.single { it.path == "cas2Application.submittedApplication.latestAssessmentStatus" }
      val tierScore = DomainDataFieldCatalog.fieldsFor(setOf(DomainDataField.TIER_SCORE)).single()

      assertThat(assessmentStatus.options).contains("Not available", "Awaiting decision", "Cancelled")
      assertThat(fields.single { it.path == "tierScore" }).isEqualTo(tierScore)
      assertThat(tierScore.options.drop(1).map { it.substringBefore(" — ") })
        .containsExactlyElementsOf(TierScore.entries.map { it.name })
      assertThat(tierScore.options).containsExactly(
        "Not available",
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "MISSING — Missing",
        "NOT_SUPERVISED — Not supervised",
      )
      assertThat(fields.map { it.path }).contains(
        "dob",
        "sexualOffenceRegistrations.sexualOffenceRegistrations",
        "currentAccommodation.endDate",
      )

      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("example", inputs))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS2))
        .build()
      val html = RulesGraphHtmlRenderer.render(listOf(RulesGraphWalker().walk("CAS2", root)))

      assertThat(html).contains(
        "\"domainData\":",
        "\"path\":\"dob\"",
        "\"label\":\"Date of birth\"",
        "\"Awaiting decision\"",
        "Domain data affecting this service",
        "Available options",
      )
    }

    @Test
    fun `render includes mermaid nodes, edges and rule catalogue`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("FAIL if example"))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("## EXAMPLE")
      assertThat(markdown).contains("flowchart TD")
      assertThat(markdown).contains("ExampleEligibility(\"<div style='text-align:left'>FAIL if example</div>\")")
      assertThat(markdown).contains("ExampleEligibility -->|PASS| confirmed")
      assertThat(markdown).contains("ExampleEligibility -->|FAIL| CAS1_NOT_ELIGIBLE")
      assertThat(markdown).contains("linkStyle 0 stroke:#2e8b2e,stroke-width:2px")
      assertThat(markdown).contains("linkStyle 1 stroke:#c0392b,stroke-width:2px")
      assertThat(markdown).contains("CAS1_NOT_ELIGIBLE[\"<b>Not Eligible</b>\"]")
      assertThat(markdown).contains("classDef ruleSetNode fill:#dbe9ff")
      assertThat(markdown).contains("classDef outcomeNode fill:#d9f2d9")
      assertThat(markdown).contains("classDef updaterNode fill:#fff2cc")
      assertThat(markdown).contains("class ExampleEligibility ruleSetNode")
      assertThat(markdown).contains("class confirmed,CAS1_NOT_ELIGIBLE outcomeNode")
      assertThat(markdown).doesNotContain("_onFail")
      assertThat(markdown).doesNotContain("[ExampleEligibility](#EXAMPLE-ExampleEligibility)")
      assertThat(markdown).containsPattern("(?m)^.*`StubRule`.*: FAIL if example")
      assertThat(markdown).doesNotContain("- FAIL:")
      assertThat(markdown).contains("| StubRule | FAIL if example | ExampleEligibility | EXAMPLE |")
    }

    @Test
    fun `render links to source files using each class package`() {
      val root = builder
        .ruleSet(
          StubRuleSet(rules = listOf(Cas1NoReferralRule(), NoLiveCrsRule())),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(Cas1StartContextUpdater())
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("EXAMPLE", root)))

      assertThat(markdown).contains(
        "[`Cas1NoReferralRule`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/Cas1NoReferralRule.kt)",
      )
      assertThat(markdown).contains(
        "[`NoLiveCrsRule`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/rules/NoLiveCrsRule.kt)",
      )
      assertThat(markdown).contains(
        "[`Cas1StartContextUpdater`](../query/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/query/eligibility/domain/contextupdaters/Cas1StartContextUpdater.kt)",
      )
    }

    @Test
    fun `HTML and markdown use the same node and edge colours`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("example"))))
        .onPass(builder.confirmed()).applyContextUpdaterOnPass(StubContextUpdater())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()
      val graphs = listOf(RulesGraphWalker().walk("EXAMPLE", root))
      val html = RulesGraphHtmlRenderer.render(graphs)
      val markdown = RulesGraphMarkdownRenderer.render(graphs)

      Regex("""classDef \w+ fill:(#[0-9a-f]+),stroke:(#[0-9a-f]+),color:(#[0-9a-f]+)""")
        .findAll(markdown).forEach { match ->
          assertThat(html).contains(
            "'background-color': '${match.groupValues[1]}'",
            "'border-color': '${match.groupValues[2]}'",
            "'color': '${match.groupValues[3]}'",
          )
        }
      Regex("""linkStyle [\d,]+ stroke:(#[0-9a-f]+),""").findAll(markdown).forEach { match ->
        assertThat(html).contains(
          "'line-color': '${match.groupValues[1]}'",
          "'target-arrow-color': '${match.groupValues[1]}'",
        )
      }
    }

    @Test
    fun `HTML simulation rule descriptions link to their source`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("FAIL if example"))))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()

      val html = RulesGraphHtmlRenderer.render(listOf(RulesGraphWalker().walk("EXAMPLE", root)))
      val simulationRules = html.substringAfter("n.rules.forEach(r => {").substringBefore("html += '</div>';")

      assertThat(simulationRules).contains(
        """data-rule="' + esc(r.className) + '">""",
        """(r.sourcePath ? '<a href="' + esc(r.sourcePath) + '" target="_blank" rel="noopener noreferrer">' +""",
        """esc(r.description) + '</a>' : esc(r.description))""",
      )
      assertThat(simulationRules).doesNotContain("<code>", "<br>", "data-source")
    }

    @Test
    fun `HTML embeds standalone example status cards for simulation outcomes`() {
      val html = RulesGraphHtmlRenderer.render(emptyList())

      assertThat(html).contains(
        "function statusCardPreview(outcome, failureReasons)",
        "function exampleCardDetails(service, status)",
        "outcomes.map(o => statusCardPreview(o, reasons) + statusActionPreview(o))",
        "Start an approved premises (CAS1) application",
        "<b>Action:</b>",
        "\"actionType\":\"START_APPROVED_PREMISE_APPLICATION\"",
        "Approved premises (CAS1)",
        "aria-disabled=\"true\"",
        "\"CAS1_NOT_STARTED\"",
        "\"CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS\"",
      )
      assertThat(html).doesNotContain("__STATUS_CARD_PREVIEW__", "__GRAPH_DATA__")
    }

    @Test
    fun `HTML shows failures and outcome cards in a flat trace above the rules`() {
      val root = builder
        .ruleSet(StubRuleSet(name = "Tier", rules = listOf(MaleRiskEligibilityRule())))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1))
        .build()

      val html = RulesGraphHtmlRenderer.render(listOf(RulesGraphWalker().walk("EXAMPLE", root)))

      assertThat(html).contains("\"failureReason\":\"MALE_NOT_HIGH_RISK_TIER\"")
      assertThat(html).contains(
        "return 'Failed because '",
        "sourceTextLink(failureDescription([rule]), rule.sourcePath) + ' ' + badge('FAIL')",
        "'Passed because ' + step.node.title.replace",
        "sourceTextLink(description, step.node.sourcePath)",
        "sourceTextLink(n.title, n.sourcePath)",
        "resultHtml(res) + '</div></li>'",
        """(outcomeCount > 1 ? 'Outcomes' : 'Outcome')""",
        "<h2>Trace</h2><ol",
        "<h2>Rules</h2>",
      )
      assertThat(html).doesNotContain("data-section", "Reasons for failure:", "<b>Outcome:</b>")
    }

    @Test
    fun `HTML links rules and PASS and FAIL context updaters to GitHub source`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(Cas1NoReferralRule())))
        .onPass(builder.confirmed()).applyContextUpdaterOnPass(Cas1StartContextUpdater())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(StubContextUpdater())
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val ruleSetNode = graph.nodes.single { it.kind == GraphNodeKind.RULE_SET }
      val sourcePaths = ruleSetNode.rules.map { it.sourcePath } + listOf(
        ruleSetNode.onPassContextUpdater?.sourcePath,
        ruleSetNode.onFailContextUpdater?.sourcePath,
      )

      val html = RulesGraphHtmlRenderer.render(listOf(graph))

      assertThat(sourcePaths).doesNotContainNull()
      sourcePaths.forEach { path ->
        assertThat(html).contains(
          "https://github.com/ministryofjustice/hmpps-single-accommodation-service-api/blob/main/${path?.removePrefix("../")}",
        )
      }
      assertThat(html).doesNotContain("../query/src/")
    }

    @Test
    fun `render lists named PASS context updater below the diagram and in the catalogue`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("window")), name = "Upcoming"))
        .onPass(builder.confirmed()).applyContextUpdaterOnPass(StubContextUpdater())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("Upcoming -->|PASS| updater_StubContextUpdater")
      assertThat(markdown).contains("class updater_StubContextUpdater_confirmed updaterNode")
      assertThat(markdown).doesNotContain(")onPass")
      assertThat(markdown).doesNotContain("[`StubContextUpdater`](#stubcontextupdater)")
      assertThat(markdown).contains("Used by: Upcoming (EXAMPLE)")
    }

    @Test
    fun `render lists named FAIL context updater below the diagram and in the catalogue`() {
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("window")), name = "Upcoming"))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).applyContextUpdaterOnFailure(StubContextUpdater())
        .build()
      val graph = RulesGraphWalker().walk("EXAMPLE", root)
      val markdown = RulesGraphMarkdownRenderer.render(listOf(graph))

      assertThat(markdown).contains("Upcoming -->|FAIL| updater_StubContextUpdater")
      assertThat(markdown).doesNotContain("_onFail")
      assertThat(markdown).doesNotContain("[`StubContextUpdater`](#stubcontextupdater)")
      assertThat(markdown).contains("Used by: Upcoming (EXAMPLE)")
    }

    @Test
    fun `render describes constant FAIL updater without a mermaid node`() {
      val root = builder
        .ruleSet(
          StubRuleSet(rules = listOf(StubRule("complete")), name = "PaCompletion"),
        )
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED))
        .build()
      val markdown = RulesGraphMarkdownRenderer.render(listOf(RulesGraphWalker().walk("PA", root)))

      assertThat(markdown).contains("PaCompletion -->|FAIL| updater_constant_CAS1_NOT_STARTED")
      assertThat(markdown).doesNotContain("_onFail")
      assertThat(markdown).contains("- FAIL: Set current status to Not Started")
      assertThat(markdown).contains("Used by: PaCompletion (PA)")
    }

    @Test
    fun `anonymous rules fail the walk`() {
      val anonymous = object : Rule<DomainData> {
        override val inputFields: Set<DomainDataField> = emptySet()
        override val description = "anon"
        override fun evaluate(data: DomainData) = RuleResult(description, RuleStatus.PASS)
      }
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(anonymous)))
        .onPass(builder.confirmed())
        .onFail(builder.notEligible(ServiceType.CAS1)).propagateContextOnFailure()
        .build()

      assertThatThrownBy { RulesGraphWalker().walk("BROKEN", root) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("anonymous")
    }

    @Test
    fun `distinct outcome statuses sharing a UI label get distinct node ids`() {
      val notStarted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED))
      val notStartedNew = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED_NEW))
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("root")), name = "Root"))
        .onPass(notStarted)
        .onFail(notStartedNew)
        .build()

      val graph = RulesGraphWalker().walk("SHARED_LABEL", root)

      val outcomes = graph.nodes.filter { it.kind == GraphNodeKind.OUTCOME }
      assertThat(outcomes.map { it.title }).containsExactly("Not Started", "Not Started")
      assertThat(outcomes.map { it.id }).containsExactly("CAS3_NOT_STARTED", "CAS3_NOT_STARTED_NEW")
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge("Root", "CAS3_NOT_STARTED", "PASS"),
        GraphEdge("Root", "CAS3_NOT_STARTED_NEW", "FAIL"),
      )
    }

    @Test
    fun `constant updaters for distinct statuses sharing a UI label get distinct node ids`() {
      val confirmed = builder.confirmed()
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("root")), name = "ExampleEligibility"))
        .onPassAndFail(confirmed)
        .applyContextUpdaterOnPass(ContextUpdater.constant(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED)))
        .setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED_NEW))
        .build()

      val graph = RulesGraphWalker().walk("SHARED_LABEL", root)

      val updaters = graph.nodes.filter { it.kind == GraphNodeKind.CONTEXT_UPDATER }
      assertThat(updaters.map { it.title }).containsOnly("Set current status to Not Started").hasSize(2)
      assertThat(updaters.map { it.id }).containsExactly(
        "updater_constant_CAS3_NOT_STARTED_confirmed",
        "updater_constant_CAS3_NOT_STARTED_NEW_confirmed",
      )
      assertThat(graph.edges).containsExactlyInAnyOrder(
        GraphEdge("ExampleEligibility", "updater_constant_CAS3_NOT_STARTED_confirmed", "PASS"),
        GraphEdge("updater_constant_CAS3_NOT_STARTED_confirmed", "confirmed", "CONTINUE"),
        GraphEdge("ExampleEligibility", "updater_constant_CAS3_NOT_STARTED_NEW_confirmed", "FAIL"),
        GraphEdge("updater_constant_CAS3_NOT_STARTED_NEW_confirmed", "confirmed", "CONTINUE"),
      )
    }

    @Test
    fun `duplicate node names fail the walk`() {
      val confirmed = builder.confirmed()
      val notEligible = builder.notEligible(ServiceType.CAS1)
      val first = builder
        .ruleSet(
          StubRuleSet(
            rules =
            listOf(
              StubRule("first"),
            ),
          ),
        )
        .onPass(confirmed)
        .onFail(notEligible).propagateContextOnFailure()
        .build()
      val root = builder
        .ruleSet(StubRuleSet(rules = listOf(StubRule("second"))))
        .onPass(first)
        .onFail(notEligible).propagateContextOnFailure()
        .build()

      assertThatThrownBy { RulesGraphWalker().walk("BROKEN", root) }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Duplicate graph node id")
    }
  }
}
