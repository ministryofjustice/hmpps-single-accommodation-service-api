package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.text.isNotBlank
import kotlin.text.split

class EligibilityScenariosCrsTest : EligibilityScenarioTestBase() {

  @Autowired
  private lateinit var crsTree: CrsEligibilityTreeProvider

  fun loadCrsScenarios(): List<CrsScenario> {
    val rows = CsvReader().read("/crs-eligibility-scenarios.csv")

    return rows.mapIndexed { idx, row ->
      try {
        CrsScenario(
          testCaseId = row["testCaseId"]!!,
          description = row["description"],
          referenceDate = row["referenceDate"]!!.toLocalDate(),
          sex = row["sex"]?.takeIf { it.isNotBlank() }?.let { SexCode.valueOf(it) },
          hasNextAccommodation = row["hasNextAccommodation"]!!,
          currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
          currentAccommodationSettledType = row["currentAccommodationSettledType"]?.takeIf { it.isNotBlank() }?.let { AccommodationSettledType.valueOf(it) },
          crsStatus = row["crsStatus"]?.let { CrsReferralStatus.valueOf(it) },
          expectedCrsStatus = row["expectedCrsStatus"]?.let { ServiceStatusNew.valueOf(it) },
          expectedCrsUrl = row["expectedCrsUrl"]?.takeIf { it.isNotBlank() },
          crsSubmissionDate = row["crsSubmissionDate"]?.toLocalDate(),
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
  fun `should calculate eligibility for crs for all scenarios`() {
    val scenarios = loadCrsScenarios()

    runScenarios(scenarios) { s ->

      clock.setNow(s.referenceDate)

      val currentAccommodation = s.currentAccommodationEndDate?.let {
        buildAccommodationSummaryDto(endDate = it)
      }

      val commissionedRehabilitativeServices = s.crsStatus?.let {
        buildCommissionedRehabilitativeServices(
          sentAt = s.crsSubmissionDate!!.atStartOfDay().atOffset(ZoneOffset.UTC),
          status = it,
        )
      }

      val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
        listOf(buildAccommodationSummaryDto())
      } else {
        emptyList()
      }

      val currentAccommodationTypeEntity = s.currentAccommodationSettledType?.let {
        buildAccommodationTypeEntity(settledType = it)
      }

      val data = buildDomainData(
        crn = s.testCaseId,
        sex = s.sex,
        currentAccommodation = currentAccommodation,
        nextAccommodations = nextAccommodations,
        commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
      )

      val result = eligibilityService.evaluate(crsTree, data)

      assertThat(result.serviceStatus)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCrsStatus}")
        .isEqualTo(s.expectedCrsStatus)

      assertThat(result.actionStartDate).isEqualTo(
        if (result.serviceStatus.isUpcoming) {
          s.currentAccommodationEndDate?.minusWeeks(12)
        } else {
          null
        },
      )
      assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CRS)
      val expectedUrl = if (s.expectedCrsUrl == "CRS_UI_URL") CRS_UI_URL else null
      assertThat(result.url).isEqualTo(expectedUrl)
      assertThat(result.failureReasons)
        .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
        .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
    }
  }

  data class CrsScenario(
    val testCaseId: String,
    val description: String?,
    val referenceDate: LocalDate,
    val sex: SexCode?,
    val hasNextAccommodation: String,
    val currentAccommodationEndDate: LocalDate?,
    val currentAccommodationSettledType: AccommodationSettledType?,
    val crsSubmissionDate: LocalDate?,
    val crsStatus: CrsReferralStatus?,
    val expectedCrsStatus: ServiceStatusNew?,
    val expectedCrsUrl: String?,
    val expectedFailureReasons: List<FailureReason>,
  )
}
