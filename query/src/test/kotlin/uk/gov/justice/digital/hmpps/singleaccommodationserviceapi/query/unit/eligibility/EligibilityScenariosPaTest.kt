package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.currentOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import kotlin.text.isNotBlank
import kotlin.text.split

class EligibilityScenariosPaTest : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var paTree: PaEligibilityTreeProvider

  fun loadPaScenarios(): List<PaScenario> {
    val rows = CsvReader().read("/pa-eligibility-scenarios.csv")

    return rows.mapIndexed { idx, row ->
      try {
        PaScenario(
          testCaseId = row["testCaseId"]!!,
          description = row["description"],
          hasNextAccommodation = row["hasNextAccommodation"]!!,
          isSubmittedCas1 = row["isSubmittedCas1"]!!,
          isSubmittedCas3 = row["isSubmittedCas3"]!!,
          expectedPaStatus = row["expectedPaStatus"]?.let { ServiceStatusNew.valueOf(it) },
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
  fun `should calculate eligibility for pa for all scenarios`() {
    val scenarios = loadPaScenarios()

    runScenarios(scenarios) { s ->

      val cas1Application = if (s.isSubmittedCas1.toBoolean()) {
        buildCas1ApplicationV2(
          status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
          submittedApplication = buildCas1SubmittedApplication(
            upcomingRequestForPlacement = buildCas1ExternalRequestForPlacementOutcomeDto(
              requestForPlacement = buildCas1RequestForPlacementSummaryV2(
                status = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
              ),
              placement = buildCas1PlacementV2(status = Cas1PlacementStatus.UPCOMING),
            ),
          ),
        )
      } else {
        null
      }

      val cas3Application = if (s.isSubmittedCas3.toBoolean()) {
        buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
            latestBooking = buildCas3LatestBooking(
              status = Cas3BookingStatus.CONFIRMED,
            ),
          ),
        )
      } else {
        null
      }

      val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
        listOf(buildAccommodationSummaryDto())
      } else {
        emptyList()
      }

      val data = buildDomainData(
        crn = s.testCaseId,
        nextAccommodations = nextAccommodations,
        cas1Application = cas1Application?.let {
          buildCas1ApplicationDomainData(
            it,
            currentOutcome = it.currentOutcome,
          )
        },
        cas3Application = cas3Application,
      )

      val result = eligibilityService.evaluate(paTree, data.toPaDomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedPaStatus}")
        .isEqualTo(s.expectedPaStatus)

      assertThat(result.actionStartDate).isNull()
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.PA)
      assertThat(result.url).isNull()

      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  data class PaScenario(
    val testCaseId: String,
    val description: String?,
    val hasNextAccommodation: String,
    val isSubmittedCas1: String,
    val isSubmittedCas3: String,
    val expectedPaStatus: ServiceStatusNew?,
    val expectedFailureReasons: List<FailureReason>,
  )
}
