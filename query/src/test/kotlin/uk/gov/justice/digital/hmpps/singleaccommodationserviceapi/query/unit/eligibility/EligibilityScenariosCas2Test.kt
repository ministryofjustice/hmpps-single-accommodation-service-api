package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas2EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.text.isNotBlank
import kotlin.text.lowercase
import kotlin.text.split
import kotlin.text.toBoolean

private const val UPCOMING_DAYS = 15L

class EligibilityScenariosCas2Test : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var cas2Tree: Cas2EligibilityTreeProvider

  fun loadCas2EligibilityScenarios(): List<Cas2EligibilityScenario> {
    val rows = CsvReader().read("/cas2-eligibility-scenarios.csv")

    return rows.flatMapIndexed { idx, row ->

      val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()
      val tierScores = row["tierScore"].expandTierScore()

      try {
        accommodationTypes.flatMap { accommodationType ->
          tierScores.map { tierScore ->
            Cas2EligibilityScenario(
              testCaseId = row["testCaseId"]!!,
              description = row["description"],
              referenceDate = row["referenceDate"]!!.toLocalDate(),
              riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
              hasNextAccommodation = row["hasNextAccommodation"]!!,
              currentAccommodationType = accommodationType,
              currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
              tierScore = tierScore,
              expectedCas2Status = row["expectedCas2Status"]?.let { ServiceStatusNew.valueOf(it) },
              expectedCas2Url = row["expectedCas2Url"],
              u18 = row["u18"]?.toBoolean(),
              hasSexualOffenceRegistrations = row["hasSexualOffenceRegistrations"]!!.lowercase().toBooleanStrict(),
              expectedFailureReasons = row["expectedFailureReasons"]
                ?.takeIf { it.isNotBlank() }
                ?.split(",")
                ?.map { FailureReason.valueOf(it.trim()) }
                ?: emptyList(),
            )
          }
        }
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }
  }

  fun loadCas2ApplicationScenarios(): List<Cas2ApplicationScenario> {
    val rows = CsvReader().read("/cas2-application-scenarios.csv")

    return rows.flatMapIndexed { idx, row ->

      val applicationStatuses = row["cas2ApplicationStatus"].expandCas2ApplicationStatus()

      try {
        applicationStatuses.map { applicationStatus ->
          Cas2ApplicationScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
            submittedAt = row["submittedAt"]?.toLocalDate(),
            cas2ApplicationStatus = applicationStatus,
            expectedCas2Status = row["expectedCas2Status"]?.let { ServiceStatusNew.valueOf(it) },
            expectedCas2Url = row["expectedCas2Url"],
            markedAsArrived = row["markedAsArrived"]!!.toBoolean(),
          )
        }
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }
  }

  private fun String?.expandCas2ApplicationStatus() = when (this) {
    "any" -> Cas2AssessmentStatus.entries.toList()
    null -> listOf(null)
    else -> listOf(Cas2AssessmentStatus.forValue(this))
  }

  private fun String?.expandTierScore() = when (this) {
    "A-G" -> listOf("A", "B", "C", "D", "E", "F", "G")
    "MISSING-OR-NOT-SUPERVISED" -> listOf("MISSING", "NOT_SUPERVISED")
    else -> listOf(this)
  }

  @Test
  fun `should calculate eligibility for cas2 for all scenarios`() {
    val scenarios = loadCas2EligibilityScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val currentAccommodation = s.currentAccommodationType?.let {
        buildAccommodationSummaryDto(
          endDate = s.currentAccommodationEndDate,
          type = buildAccommodationTypeDto(
            code = s.currentAccommodationType,
          ),
        )
      }

      val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
        listOf(buildAccommodationSummaryDto())
      } else {
        emptyList()
      }

      val aged18 = LocalDate.now(clock).minusYears(18)
      val under18 = LocalDate.now(clock).minusYears(18).plusDays(1)

      val data = buildDomainData(
        crn = s.testCaseId,
        currentAccommodation = currentAccommodation,
        nextAccommodations = nextAccommodations,
        cas2Application = null,
        currentAccommodationTypeEntity = s.currentAccommodationType?.let {
          buildAccommodationTypeEntity(
            code = it,
            isCas1 = it == "A02",
            isPrison = it == "HMP",
            isCas2 = it == "A10" || it == "A11",
            isCas3 = it == "A17",
          )
        },
        dob = if (s.u18 == null) {
          null
        } else if (s.u18) {
          under18
        } else {
          aged18
        },
        tierScore = s.tierScore,
        sexualOffenceRegistrations = if (s.hasSexualOffenceRegistrations) {
          buildSexualOffenceRegistrations(crn = s.testCaseId)
        } else {
          null
        },
        roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
      )

      val result = eligibilityService.evaluate(cas2Tree, data)

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas2Status}")
        .isEqualTo(s.expectedCas2Status)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS2)
      val expectedUrl = when (s.expectedCas2Url) {
        null -> null
        "/applications/start" -> "CAS2_APPLICATION_START_URL${s.testCaseId}"
        else -> error("Unexpected CAS2 eligibility URL: ${s.expectedCas2Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)

      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  @Test
  fun `should calculate application for cas2 for all scenarios`() {
    val scenarios = loadCas2ApplicationScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val cas2Application = buildCas2Application(
        submittedApplication = if (s.submittedAt != null) {
          buildCas2SubmittedApplicationSummary(
            latestAssessmentStatus = s.cas2ApplicationStatus,
            markedArrivedDateTime = if (s.markedAsArrived) {
              s.referenceDate.atStartOfDay().toInstant(ZoneOffset.UTC)
            } else {
              null
            },
            submittedAt = s.submittedAt.atStartOfDay().atOffset(ZoneOffset.UTC),
            cancelledReason = if (s.cas2ApplicationStatus == Cas2AssessmentStatus.CANCELLED) {
              "cancelled reason"
            } else {
              null
            },
            offerDeclinedReason = if (s.cas2ApplicationStatus == Cas2AssessmentStatus.OFFER_DECLINED) {
              "offerDeclined reason"
            } else {
              null
            },
          )
        } else {
          null
        },
      )

      val data = buildDomainData(
        crn = s.testCaseId,
        currentAccommodation = buildAccommodationSummaryDto(
          endDate = s.currentAccommodationEndDate,
          type = buildAccommodationTypeDto(
            code = "A02",
          ),
        ),
        nextAccommodations = emptyList(),
        cas2Application = cas2Application,
        currentAccommodationTypeEntity = buildAccommodationTypeEntity(
          code = "A02",
          isCas1 = true,
          isCas2 = !s.isEligible,
        ),
        dob = LocalDate.now(clock).minusYears(18),
        tierScore = "A",
        sexualOffenceRegistrations = null,
        roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
      )

      val result = eligibilityService.evaluate(cas2Tree, data)

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas2Status}")
        .isEqualTo(s.expectedCas2Status)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS2)
      val expectedUrl = when (s.expectedCas2Url) {
        null -> null
        "/applications/start" -> "CAS2_APPLICATION_START_URL${s.testCaseId}"
        "/applications/{applicationId}" -> cas2Application.uiUrl
        else -> error("Unexpected CAS2 application URL: ${s.expectedCas2Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)
    }
  }

  data class Cas2EligibilityScenario(
    val testCaseId: String,
    val description: String?,
    val hasNextAccommodation: String,
    val tierScore: String?,
    val referenceDate: LocalDate,
    val riskLevel: RiskLevel?,
    val currentAccommodationType: String?,
    val currentAccommodationEndDate: LocalDate?,
    val expectedCas2Status: ServiceStatusNew?,
    val expectedCas2Url: String?,
    val u18: Boolean?,
    val hasSexualOffenceRegistrations: Boolean,
    val expectedFailureReasons: List<FailureReason>,
  )

  data class Cas2ApplicationScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val currentAccommodationEndDate: LocalDate?,
    val isEligible: Boolean,
    val submittedAt: LocalDate?,
    val cas2ApplicationStatus: Cas2AssessmentStatus?,
    val expectedCas2Status: ServiceStatusNew?,
    val expectedCas2Url: String?,
    val markedAsArrived: Boolean,
  )
}
