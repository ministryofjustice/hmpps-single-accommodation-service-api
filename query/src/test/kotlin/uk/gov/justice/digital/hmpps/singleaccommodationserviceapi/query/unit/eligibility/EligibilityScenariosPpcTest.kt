package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPublicProtectionCheckerEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.PpcEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader

class EligibilityScenariosPpcTest : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var ppcTree: PpcEligibilityTreeProvider

  private fun loadPpcScenarios(): List<PpcScenario> {
    val rows = CsvReader().read("/ppc-eligibility-scenarios.csv")
    return rows.map { row ->
      val supervisionRequired = row.getValue("supervisionRequired")?.toBooleanStrict()
      val controlMeasuresSuitable = row.getValue("controlMeasuresSuitable")?.toBooleanStrict()
      val practicalSupportSufficient = row.getValue("practicalSupportSufficient")?.toBooleanStrict()
      val answers = when {
        supervisionRequired == null && controlMeasuresSuitable == null && practicalSupportSufficient == null -> null
        supervisionRequired != null && controlMeasuresSuitable != null && practicalSupportSufficient != null ->
          Triple(supervisionRequired, controlMeasuresSuitable, practicalSupportSufficient)

        else -> error("PPC scenario ${row["testCaseId"]} must supply all three answers or none")
      }

      PpcScenario(
        testCaseId = row.getValue("testCaseId"),
        description = row.getValue("description"),
        answers = answers,
        expectedPpcStatus = ServiceStatusV2.valueOf(row.getValue("expectedPpcStatus")),
      )
    }
  }

  @Test
  fun `should calculate PPC eligibility for all scenarios`() {
    val scenarios = loadPpcScenarios()
    val booleanValues = listOf(false, true)
    val allSubmittedAnswers = booleanValues.flatMap { supervisionRequired ->
      booleanValues.flatMap { controlMeasuresSuitable ->
        booleanValues.map { practicalSupportSufficient ->
          Triple(supervisionRequired, controlMeasuresSuitable, practicalSupportSufficient)
        }
      }
    }
    assertThat(scenarios.map { it.answers })
      .containsExactlyInAnyOrderElementsOf(allSubmittedAnswers + listOf(null))

    runScenarios(scenarios) { scenario ->
      val publicProtectionChecker = scenario.answers?.let { (supervisionRequired, controlMeasuresSuitable, practicalSupportSufficient) ->
        buildPublicProtectionCheckerEntity(
          supervisionRequired = supervisionRequired,
          controlMeasuresSuitable = controlMeasuresSuitable,
          practicalSupportSufficient = practicalSupportSufficient,
        )
      }
      val data = buildDomainData(crn = scenario.testCaseId, publicProtectionChecker = publicProtectionChecker)
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
    val answers: Triple<Boolean, Boolean, Boolean>?,
    val expectedPpcStatus: ServiceStatusV2,
  )
}
