package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RenderPath
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphHtmlRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphRenderer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.bootAndGenerate
import java.io.File

class EligibilityRulesGraphGenerationTest {

  @Test
  fun `standalone graph generation passes all eligibility trees to the renderer`() {
    bootAndGenerate(
      renderPaths = listOf(
        RenderPath(
          path = File.createTempFile("prefix", "suffix").toPath(),
          renderer = RulesGraphRenderer { graphs ->
            assertThat(graphs.map { it.treeName }).containsExactly(
              "CAS1",
              "CAS2",
              "CAS3",
              "CRS",
              "DTR",
              "PA",
              "PPC",
            )
            graphs.forEach { graph ->
              assertThat(graph.nodes).isNotEmpty()
              assertThat(graph.edges).isNotEmpty()
            }
            val html = RulesGraphHtmlRenderer.render(graphs)
            assertThat(html).contains(
              "Choose a service to see its tree and explore its rule sets, context updaters and outcomes",
              "<h2>' + esc(t.name) + '</h2>'",
              "<p class=\"full-name\">' + esc(t.fullName) + '</p>",
              "data-tree=\"' + esc(t.name)",
            )
            val json = html.substringAfter("<script type=\"application/json\" id=\"graph-data\">").substringBefore("</script>")
            val trees = JsonMapper.builder().build().readTree(json)["trees"].associateBy { it["name"].asText() }
            assertThat(trees.getValue("CAS1")["description"].asText())
              .endsWith("from application and assessment through placement request and status.")
            assertThat(trees.getValue("CAS2")["fullName"].asText()).isEqualTo("Short-term accommodation (ISR)")
            assertThat(trees.getValue("CAS3")["fullName"].asText()).isEqualTo("Temporary accommodation")
            assertThat(trees.getValue("PA")["fullName"].asText()).isEqualTo("Proposed accommodation")
            assertThat(trees.getValue("PPC")["fullName"].asText()).isEqualTo("Public protection checker")
            assertThat(trees.getValue("PPC")["description"].asText())
              .isEqualTo("Reports whether the public protection checker has been completed and whether a protection need was identified.")
            graphs.forEach { graph -> assertThat(trees.getValue(graph.treeName)["fullName"].isTextual).isTrue() }
            ""
          },
        ),
      ),
    )
  }
}
