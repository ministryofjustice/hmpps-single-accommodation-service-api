package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CAS1_UPCOMING_WEEKS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.currentOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import java.time.LocalDate
import kotlin.text.isNotBlank
import kotlin.text.lowercase
import kotlin.text.split

class EligibilityScenariosCas1Test : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var cas1Tree: Cas1EligibilityTreeProvider

  fun loadCas1EligibilityScenarios(): List<Cas1EligibilityScenario> {
    val rows = CsvReader().read("/cas1-eligibility-scenarios.csv")

    return rows.flatMapIndexed { idx, row ->
      val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()

      try {
        accommodationTypes.map { accommodationType ->
          Cas1EligibilityScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
            sex = row["sex"]?.let { SexCode.valueOf(it) },
            tierScore = row["tierScore"]?.let(TierScore::valueOf),
            currentAccommodationType = accommodationType,
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            expectedCas1Status = row["expectedCas1Status"]?.let { ServiceStatusNew.valueOf(it) },
            expectedCas1Url = row["expectedCas1Url"],
            expectedFailureReasons = row["expectedFailureReasons"]
              ?.takeIf { it.isNotBlank() }
              ?.split(",")
              ?.map { FailureReason.valueOf(it.trim()) }
              ?: emptyList(),
          )
        }
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }
  }

  fun loadCas1ApplicationScenarios(): List<Cas1ApplicationScenario> = CsvReader().read("/cas1-application-scenarios.csv").mapIndexed { idx, row ->
    try {
      Cas1ApplicationScenario(
        testCaseId = row["testCaseId"]!!,
        description = row["description"],
        referenceDate = row["referenceDate"]!!.toLocalDate(),
        currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
        isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
        cas1ApplicationStatus = row["cas1ApplicationStatus"]?.let { Cas1ApplicationStatus.valueOf(it) },
        cas1RequestForPlacementStatus = row["cas1RequestForPlacementStatus"]?.let {
          Cas1RequestForPlacementStatus.valueOf(it)
        },
        cas1PlacementStatus = row["cas1PlacementStatus"]?.let { Cas1PlacementStatus.valueOf(it) },
        expectedCas1Status = row["expectedCas1Status"]?.let { ServiceStatusNew.valueOf(it) },
        expectedCas1Url = row["expectedCas1Url"],
      )
    } catch (e: Exception) {
      throw IllegalStateException("Row $idx failed: $row", e)
    }
  }

  @Test
  fun `should calculate eligibility for cas1 for all scenarios`() {
    val scenarios = loadCas1EligibilityScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val currentAccommodation = s.currentAccommodationType?.let {
        buildAccommodationSummaryDto(
          type = buildAccommodationTypeDto(code = it),
          endDate = s.currentAccommodationEndDate,
        )
      }

      val currentAccommodationTypeEntity = s.currentAccommodationType?.let {
        buildAccommodationTypeEntity(
          code = it,
          isPrison = it == "HMP",
          isCas1 = it == "A02",
        )
      }

      val data = buildDomainData(
        crn = s.testCaseId,
        tierScore = s.tierScore,
        sex = s.sex,
        currentAccommodation = currentAccommodation,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        cas1Application = null,
        roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
      )

      val result = eligibilityService.evaluate(cas1Tree, data.toCas1DomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
        .isEqualTo(s.expectedCas1Status)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusWeeks(CAS1_UPCOMING_WEEKS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS1)
      val expectedUrl = when (s.expectedCas1Url) {
        null -> null
        "/applications/start" -> CAS1_APPLICATION_START_URL
        else -> error("Unexpected CAS1 eligibility URL: ${s.expectedCas1Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)

      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  @Test
  fun `should calculate application for cas1 for all scenarios`() {
    val scenarios = loadCas1ApplicationScenarios()

    runScenarios(scenarios) { s ->
      clock.setNow(s.referenceDate)

      val cas1Application = s.cas1ApplicationStatus?.let {
        buildCas1ApplicationV2(
          status = it,
          submittedApplication = buildCas1SubmittedApplication(
            upcomingRequestForPlacement = if (s.cas1RequestForPlacementStatus != null || s.cas1PlacementStatus != null) {
              buildCas1ExternalRequestForPlacementOutcomeDto(
                requestForPlacement = buildCas1RequestForPlacementSummaryV2(
                  status = s.cas1RequestForPlacementStatus!!,
                ),
                placement = s.cas1PlacementStatus?.let { status -> buildCas1PlacementV2(status = status) },
              )
            } else {
              null
            },
          ),
        )
      }

      val data = buildDomainData(
        crn = s.testCaseId,
        tierScore = if (s.isEligible) TierScore.A else TierScore.D,
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(
          type = buildAccommodationTypeDto(code = "A02"),
          endDate = s.currentAccommodationEndDate,
        ),
        currentAccommodationTypeEntity = buildAccommodationTypeEntity(code = "A02", isCas1 = true),
        cas1Application = cas1Application?.let { buildCas1ApplicationDomainData(it, currentOutcome = it.currentOutcome) },
        roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
      )

      val result = eligibilityService.evaluate(cas1Tree, data.toCas1DomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
        .isEqualTo(s.expectedCas1Status)
      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusWeeks(CAS1_UPCOMING_WEEKS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS1)
      val expectedUrl = when (s.expectedCas1Url) {
        null -> null
        "/applications/start" -> CAS1_APPLICATION_START_URL
        "/applications/{applicationId}" -> cas1Application?.uiUrl
        else -> error("Unexpected CAS1 application URL: ${s.expectedCas1Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)
    }
  }

  data class Cas1EligibilityScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val riskLevel: RiskLevel?,
    val sex: SexCode?,
    val tierScore: TierScore?,
    val currentAccommodationType: String?,
    val currentAccommodationEndDate: LocalDate?,
    val expectedCas1Status: ServiceStatusNew?,
    val expectedCas1Url: String?,
    val expectedFailureReasons: List<FailureReason>,
  )

  data class Cas1ApplicationScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val currentAccommodationEndDate: LocalDate?,
    val isEligible: Boolean,
    val cas1ApplicationStatus: Cas1ApplicationStatus?,
    val cas1RequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    val cas1PlacementStatus: Cas1PlacementStatus?,
    val expectedCas1Status: ServiceStatusNew?,
    val expectedCas1Url: String?,
  )
}
