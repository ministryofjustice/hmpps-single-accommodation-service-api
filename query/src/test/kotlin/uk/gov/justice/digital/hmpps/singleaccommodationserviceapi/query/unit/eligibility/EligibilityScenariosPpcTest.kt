package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.PpcEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.PublicProtectionCheckerAnswers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader

class EligibilityScenariosPpcTest : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var ppcTree: PpcEligibilityTreeProvider

  private fun loadPpcScenarios(): List<PpcScenario> {
    val rows = CsvReader().read("/ppc-eligibility-scenarios.csv")
    return rows.map { row ->
      val supervisionRequired = row.getValue("supervisionRequired")?.toBooleanStrict()
      val controlMeasureSuitable = row.getValue("controlMeasureSuitable")?.toBooleanStrict()
      val practicalSupportSufficient = row.getValue("practicalSupportSufficient")?.toBooleanStrict()
      val answers = when {
        supervisionRequired == null && controlMeasureSuitable == null && practicalSupportSufficient == null -> null
        supervisionRequired != null && controlMeasureSuitable != null && practicalSupportSufficient != null ->
          PublicProtectionCheckerAnswers(supervisionRequired, controlMeasureSuitable, practicalSupportSufficient)

        else -> error("PPC scenario ${row["testCaseId"]} must supply all three answers or none")
      }

      PpcScenario(
        testCaseId = row.getValue("testCaseId"),
        description = row.getValue("description"),
        answers = answers,
        expectedPpcStatus = ServiceStatusNew.valueOf(row.getValue("expectedPpcStatus")),
      )
    }
  }

  @Test
  fun `should calculate PPC eligibility for all scenarios`() {
    val scenarios = loadPpcScenarios()
    val booleanValues = listOf(false, true)
    val allSubmittedAnswers = booleanValues.flatMap { supervisionRequired ->
      booleanValues.flatMap { controlMeasureSuitable ->
        booleanValues.map { practicalSupportSufficient ->
          PublicProtectionCheckerAnswers(supervisionRequired, controlMeasureSuitable, practicalSupportSufficient)
        }
      }
    }
    assertThat(scenarios.map { it.answers })
      .containsExactlyInAnyOrderElementsOf(allSubmittedAnswers + listOf(null))

    runScenarios(scenarios) { scenario ->
      val data = buildDomainData(crn = scenario.testCaseId, ppcAnswers = scenario.answers)
      val result = eligibilityService.evaluate(ppcTree, data)

      assertThat(result.serviceStatus).isEqualTo(scenario.expectedPpcStatus)
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.PPC)
      assertThat(result.actionStartDate).isNull()
      assertThat(result.url).isNull()
      assertThat(result.failureReasons).isEmpty()
    }
  }

  private data class PpcScenario(
    val testCaseId: String,
    val description: String,
    val answers: PublicProtectionCheckerAnswers?,
    val expectedPpcStatus: ServiceStatusNew,
  )
}
