package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RenderPath
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
            )
            graphs.forEach { graph ->
              assertThat(graph.nodes).isNotEmpty()
              assertThat(graph.edges).isNotEmpty()
            }
            ""
          },
        ),
      ),
    )
  }
}
