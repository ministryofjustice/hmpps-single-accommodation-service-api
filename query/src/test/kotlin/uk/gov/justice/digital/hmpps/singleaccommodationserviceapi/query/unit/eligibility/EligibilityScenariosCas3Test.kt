package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBookingPremisesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.text.isNotBlank
import kotlin.text.lowercase
import kotlin.text.split

private const val UPCOMING_DAYS = 15L

class EligibilityScenariosCas3Test : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var cas3Tree: Cas3EligibilityTreeProvider

  @Test
  fun `arrived CAS3 booking ending beyond 15 days returns not eligible for current accommodation type`() {
    val referenceDate = LocalDate.of(2026, 11, 10)
    val accommodationEndDate = LocalDate.of(2027, 2, 1)
    clock.setNow(referenceDate)

    val data = buildDomainData(
      sex = SexCode.M,
      currentAccommodation = buildAccommodationSummaryDto(
        startDate = referenceDate,
        endDate = accommodationEndDate,
        type = buildAccommodationTypeDto(code = "A17"),
      ),
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(code = "A17", isCas3 = true),
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplication(
          submittedDate = LocalDate.of(2026, 11, 4),
          assessmentStatus = Cas3AssessmentStatus.CLOSED,
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.ARRIVED,
            premises = buildCas3LatestBookingPremisesDto().copy(
              startDate = referenceDate,
              endDate = accommodationEndDate,
            ),
          ),
        ),
      ),
      dutyToRefer = buildDutyToReferDto(
        submission = buildDtrSubmission(submissionDate = LocalDate.of(2026, 11, 2)),
      ),
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        sentAt = LocalDate.of(2026, 11, 4).atStartOfDay().atOffset(ZoneOffset.UTC),
        status = CrsReferralStatus.LIVE,
      ),
      roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
    )

    // The end date is beyond 15 days, so the tree bypasses the active-referral check.
    // Current CAS3 accommodation is not prison, CAS1 or CAS2, so it fails before DTR/CRS prerequisites.
    val result = eligibilityService.evaluate(cas3Tree, data.toCas3DomainData())

    assertThat(result).isEqualTo(
      ServiceResultNew(
        serviceStatus = ServiceStatusNew.CAS3_NOT_ELIGIBLE,
        failureReasons = listOf(FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE),
      ),
    )
  }

  fun loadCas3ApplicationScenarios(): List<Cas3ApplicationScenario> {
    val rows = CsvReader().read("/cas3-application-scenarios.csv")

    return rows.mapIndexed { idx, row ->
      try {
        Cas3ApplicationScenario(
          testCaseId = row["testCaseId"]!!,
          description = row["description"],
          referenceDate = row["referenceDate"]!!.toLocalDate(),
          currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
          expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatusNew.valueOf(it) },
          expectedCas3Url = row["expectedCas3Url"]?.takeIf { it.isNotBlank() },
          isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
          isStarted = row["isStarted"]!!.lowercase().toBooleanStrict(),
          cas3ApplicationStatus = row["cas3ApplicationStatus"]?.let { Cas3ApplicationStatus.valueOf(it) },
          cas3AssessmentStatus = row["cas3AssessmentStatus"]?.let { Cas3AssessmentStatus.valueOf(it) },
          cas3BookingStatus = row["cas3BookingStatus"]?.let { Cas3BookingStatus.valueOf(it) },
        )
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }
  }

  fun loadCas3EligibilityScenarios(): List<Cas3EligibilityScenario> {
    val rows = CsvReader().read("/cas3-eligibility-scenarios.csv")

    return rows.flatMapIndexed { idx, row ->
      try {
        val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()
        val sexCodes = when (val sexCode = row["sexCode"]) {
          "notM" -> listOf(SexCode.N, SexCode.F, SexCode.NS)
          null -> listOf(null)
          else -> listOf(SexCode.valueOf(sexCode))
        }
        accommodationTypes.flatMap { accommodationType ->
          sexCodes.map { sexCode ->
            Cas3EligibilityScenario(
              testCaseId = row["testCaseId"]!!,
              description = row["description"],
              referenceDate = row["referenceDate"]!!.toLocalDate(),
              riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
              hasNextAccommodation = row["hasNextAccommodation"]!!,
              currentAccommodationType = accommodationType,
              currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
              dtrSubmissionDate = row["dtrSubmissionDate"]?.toLocalDate(),
              crsStatus = row["crsStatus"]?.let { CrsReferralStatus.valueOf(it) },
              sexCode = sexCode,
              expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatusNew.valueOf(it) },
              expectedCas3Url = row["expectedCas3Url"]?.takeIf { it.isNotBlank() },
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

  @Test
  fun `should calculate application for cas3 for all scenarios`() {
    val scenarios = loadCas3ApplicationScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val cas3Application = s.cas3ApplicationStatus?.let { applicationStatus ->
        buildCas3Application(
          applicationStatus = applicationStatus,
          submittedApplication = if (applicationStatus == Cas3ApplicationStatus.IN_PROGRESS) {
            null
          } else {
            buildCas3SubmittedApplication(
              assessmentStatus = s.cas3AssessmentStatus,
              assessmentRejectionReason = "Rejected".takeIf { s.cas3AssessmentStatus == Cas3AssessmentStatus.REJECTED },
              latestBooking = s.cas3BookingStatus?.let { buildCas3LatestBooking(status = it) },
            )
          },
        )
      }

      val dutyToRefer = buildDutyToReferDto(
        submission = buildDtrSubmission(submissionDate = s.referenceDate),
      )

      val currentAccommodation = buildAccommodationSummaryDto(
        endDate = s.currentAccommodationEndDate,
        type = buildAccommodationTypeDto(code = if (s.isEligible) "HMP" else "CAS3"),
      )

      val currentAccommodationTypeEntity = if (s.isEligible) {
        buildAccommodationTypeEntity(
          code = "HMP",
          isPrison = true,
        )
      } else {
        buildAccommodationTypeEntity(
          code = "CAS3",
          isCas3 = true,
        )
      }

      val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        sentAt = s.referenceDate.atStartOfDay().atOffset(ZoneOffset.UTC),
        status = if (s.isStarted) CrsReferralStatus.LIVE else CrsReferralStatus.WITHDRAWN,
      )

      val data = buildDomainData(
        crn = s.testCaseId,
        tierScore = null,
        sex = SexCode.M,
        currentAccommodation = currentAccommodation,
        nextAccommodations = emptyList(),
        cas3Application = cas3Application,
        dutyToRefer = dutyToRefer,
        commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
      )

      val result = eligibilityService.evaluate(cas3Tree, data.toCas3DomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas3Status}")
        .isEqualTo(s.expectedCas3Status)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS3)

      val expectedUrl = when (s.expectedCas3Url) {
        null -> null
        "/referrals/start" -> CAS3_REFERRAL_START_URL
        "/referrals/{referralId}/full" -> cas3Application?.uiUrl
        else -> error("Unexpected CAS3 application URL: ${s.expectedCas3Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)
    }
  }

  @Test
  fun `should calculate eligibility for cas3 for all scenarios`() {
    val scenarios = loadCas3EligibilityScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val dutyToRefer = s.dtrSubmissionDate?.let {
        buildDutyToReferDto(
          submission = buildDtrSubmission(submissionDate = it),
        )
      }

      val currentAccommodation = s.currentAccommodationType?.let {
        buildAccommodationSummaryDto(
          endDate = s.currentAccommodationEndDate,
          type = buildAccommodationTypeDto(code = it),
        )
      }

      val currentAccommodationTypeEntity = s.currentAccommodationType?.let {
        buildAccommodationTypeEntity(
          code = it,
          isPrison = it == "HMP",
          isCas1 = it == "A02",
          isCas2 = it == "A10" || it == "A11",
          isCas3 = it == "A17",
        )
      }

      val commissionedRehabilitativeServices = s.crsStatus?.let {
        buildCommissionedRehabilitativeServices(
          sentAt = s.referenceDate.atStartOfDay().atOffset(ZoneOffset.UTC),
          status = it,
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
        sex = s.sexCode,
        currentAccommodation = currentAccommodation,
        nextAccommodations = nextAccommodations,
        cas3Application = null,
        dutyToRefer = dutyToRefer,
        commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
      )

      val result = eligibilityService.evaluate(cas3Tree, data.toCas3DomainData())

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas3Status}")
        .isEqualTo(s.expectedCas3Status)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS3)

      val expectedUrl = when (s.expectedCas3Url) {
        null -> null
        "/referrals/start" -> CAS3_REFERRAL_START_URL
        else -> error("Unexpected CAS3 eligibility URL: ${s.expectedCas3Url}")
      }
      assertThat(result.url).isEqualTo(expectedUrl)
      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  data class Cas3EligibilityScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val riskLevel: RiskLevel?,
    val hasNextAccommodation: String,
    val currentAccommodationType: String?,
    val currentAccommodationEndDate: LocalDate?,
    val dtrSubmissionDate: LocalDate?,
    val crsStatus: CrsReferralStatus?,
    val expectedCas3Status: ServiceStatusNew?,
    val sexCode: SexCode?,
    val expectedCas3Url: String?,
    val expectedFailureReasons: List<FailureReason>,
  )

  data class Cas3ApplicationScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val currentAccommodationEndDate: LocalDate?,
    val isEligible: Boolean,
    val isStarted: Boolean,
    val cas3ApplicationStatus: Cas3ApplicationStatus?,
    val cas3AssessmentStatus: Cas3AssessmentStatus?,
    val cas3BookingStatus: Cas3BookingStatus?,
    val expectedCas3Status: ServiceStatusNew?,
    val expectedCas3Url: String?,
  )
}
