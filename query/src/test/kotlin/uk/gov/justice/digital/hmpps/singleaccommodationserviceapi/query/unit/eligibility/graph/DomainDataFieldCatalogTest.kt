package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.DomainDataFieldCatalog
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.RulesGraphWalker
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.EligibilityScenarioTestBase

class DomainDataFieldCatalogTest : EligibilityScenarioTestBase() {
  @Autowired
  private lateinit var providers: List<EligibilityTreeProvider<*>>

  @Autowired
  private lateinit var rules: List<Rule<*>>

  @Test
  fun `every service derives a non-empty unique catalogue from its actual tree`() {
    val graphs = providers.map { RulesGraphWalker().walk(it) }
    assertThat(graphs.map { it.treeName }).containsExactlyInAnyOrder("CAS1", "CAS2", "CAS3", "CRS", "DTR", "PA")
    assertThat(rules).allSatisfy { assertThat(it.inputFields).describedAs(it.javaClass.simpleName).isNotEmpty() }
    graphs.forEach { graph ->
      val fields = DomainDataFieldCatalog.fieldsFor(graph.inputFields)
      assertThat(fields).describedAs(graph.treeName).isNotEmpty()
      assertThat(fields.map { it.path }).doesNotHaveDuplicates()
      assertThat(fields.map { it.path }).containsAll(graph.inputFields.map { it.path })
    }
  }

  @Test
  fun `CAS3 includes shared DTR and CRS prerequisite inputs and CRS includes updater-only sex input`() {
    val graphs = providers.map { RulesGraphWalker().walk(it) }.associateBy { it.treeName }
    val cas3 = graphs.getValue("CAS3")
    val crs = graphs.getValue("CRS")
    val dtr = graphs.getValue("DTR")

    assertThat(cas3.inputFields).contains(DomainDataField.SEX, DomainDataField.CRS_STATUS, DomainDataField.DTR_SUBMISSION_DATE)
    assertThat(crs.inputFields).contains(DomainDataField.SEX)
    assertThat(dtr.inputFields).contains(DomainDataField.DTR_SUBMISSION_DATE)

    val fields = DomainDataFieldCatalog.fieldsFor(cas3.inputFields)
    val crsFields = DomainDataFieldCatalog.fieldsFor(crs.inputFields)
    val dtrFields = DomainDataFieldCatalog.fieldsFor(dtr.inputFields)
    assertThat(fields.single { it.path == DomainDataField.SEX.path })
      .isEqualTo(crsFields.single { it.path == DomainDataField.SEX.path })
    assertThat(fields.single { it.path == DomainDataField.CRS_STATUS.path })
      .isEqualTo(crsFields.single { it.path == DomainDataField.CRS_STATUS.path })
    assertThat(fields.single { it.path == DomainDataField.DTR_SUBMISSION_DATE.path })
      .isEqualTo(dtrFields.single { it.path == DomainDataField.DTR_SUBMISSION_DATE.path })
  }
}
