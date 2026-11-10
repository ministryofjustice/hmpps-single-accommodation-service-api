package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import java.time.LocalDate
import kotlin.text.isNotBlank
import kotlin.text.split

class EligibilityScenariosDtrTest : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var dtrTree: DtrEligibilityTreeProvider

  fun loadDtrScenarios(): List<DtrScenario> {
    val rows = CsvReader().read("/dtr-eligibility-scenarios.csv")

    return rows.mapIndexed { idx, row ->
      try {
        DtrScenario(
          testCaseId = row["testCaseId"]!!,
          description = row["description"],
          referenceDate = row["referenceDate"]!!.toLocalDate(),
          currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
          hasNextAccommodation = row["hasNextAccommodation"]!!,
          dtrStatus = row["dtrStatus"]?.let { DtrStatus.valueOf(it) },
          dtrSubmissionDate = row["dtrSubmissionDate"]?.toLocalDate(),
          expectedDtrStatus = row["expectedDtrStatus"]?.let { ServiceStatusNew.valueOf(it) },
          expectedFailureReasons = row["expectedFailureReasons"]
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { FailureReason.valueOf(it.trim()) }
            ?: emptyList(),
        )
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }
  }

  @Test
  fun `should calculate eligibility for dtr for all scenarios`() {
    val scenarios = loadDtrScenarios()

    runScenarios(scenarios) { s ->
      clock.setNow(s.referenceDate)

      val dutyToRefer = s.dtrStatus?.let { it ->
        buildDutyToReferDto(
          status = it,
          submission = s.dtrSubmissionDate?.let {
            buildDtrSubmission(submissionDate = it)
          },
        )
      }

      val currentAccommodation = buildAccommodationSummaryDto(
        endDate = s.currentAccommodationEndDate,
      )

      val currentAccommodationTypeEntity = currentAccommodation.type?.code?.let {
        buildAccommodationTypeEntity(
          code = it,
        )
      }

      val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
        listOf(buildAccommodationSummaryDto())
      } else {
        emptyList()
      }

      val data = buildDomainData(
        crn = s.testCaseId,
        tierScore = null,
        sex = null,
        dutyToRefer = dutyToRefer,
        nextAccommodations = nextAccommodations,
        currentAccommodation = currentAccommodation,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
      )

      val result = eligibilityService.evaluate(dtrTree, data.toDtrDomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedDtrStatus}")
        .isEqualTo(s.expectedDtrStatus)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusWeeks(8)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.DTR)
      assertThat(result.url).isNull()
      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  data class DtrScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val currentAccommodationEndDate: LocalDate?,
    val hasNextAccommodation: String,
    val dtrStatus: DtrStatus?,
    val dtrSubmissionDate: LocalDate?,
    val expectedDtrStatus: ServiceStatusNew?,
    val expectedFailureReasons: List<FailureReason>,
  )
}
