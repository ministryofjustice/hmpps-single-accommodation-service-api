package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.bootAndGenerate

class EligibilityRulesGraphGenerationTest {

  @Test
  fun `standalone graph generation discovers all eligibility trees`() {
    bootAndGenerate { graphs ->
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
    }
  }
}
