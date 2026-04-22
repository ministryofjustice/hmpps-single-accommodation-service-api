package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CrsStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NoConflictingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.Cas3RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(value = [MockKExtension::class])
class EligibilityServiceTest {

  private val clock = MutableClock()
  private val crn = "ABC1234"
  private val male = buildSex(SexCode.M)

  private val accommodationQueryService = mockk<AccommodationQueryService>()
  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()
  private val caseRepository = mockk<CaseRepository>()

  var cas1EligibilityRuleSet = Cas1EligibilityRuleSet(
    listOf(MaleRiskEligibilityRule(), NonMaleRiskEligibilityRule(), STierEligibilityRule()),
  )
  var cas3ValidationRuleSet = Cas3ValidationRuleSet(listOf(CurrentAccommodationEndDateValidationRule()))
  var cas1ValidationRuleSet = Cas1ValidationRuleSet(listOf(CurrentAccommodationEndDateValidationRule(), Cas1SexValidationRule()))
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(listOf(Cas1ApplicationSuitabilityRule()))
  var cas1ContextUpdater = Cas1ContextUpdater(clock)
  var commonContextUpdater = CommonContextUpdater()
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(listOf(Cas1ApplicationCompletionRule()))

  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    listOf(
      CurrentAccommodationTypeRule(),
      NextAccommodationRule(),
      DtrStatusRule(),
      CrsStatusRule(),
      NoConflictingCas1BookingRule(),
    ),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(listOf(Cas3ApplicationCompletionRule(), Cas3RecentCurrentAccommodationEndDateRule(clock)))
  var cas3SuitabilityRuleSet =
    Cas3SuitabilityRuleSet(listOf(Cas3ApplicationSuitabilityRule(), Cas3RecentCurrentAccommodationEndDateRule(clock), Cas3ApplicationExpiredRule()))

  var cas3ContextUpdater = Cas3ContextUpdater(clock)

  var rulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  private val eligibilityService = EligibilityService(
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    cas1EligibilityRuleSet = cas1EligibilityRuleSet,
    cas1SuitabilityRuleSet = cas1SuitabilityRuleSet,
    cas1CompletionRuleSet = cas1CompletionRuleSet,
    cas1ValidationRuleSet = cas1ValidationRuleSet,
    cas1ContextUpdater = cas1ContextUpdater,
    commonContextUpdater = commonContextUpdater,
    cas3EligibilityRuleSet = cas3EligibilityRuleSet,
    cas3CompletionRuleSet = cas3CompletionRuleSet,
    cas3ContextUpdater = cas3ContextUpdater,
    engine = rulesEngine,
    cas3ValidationRuleSet = cas3ValidationRuleSet,
    cas3SuitabilityRuleSet = cas3SuitabilityRuleSet,
    accommodationQueryService = accommodationQueryService,
  )

  @Nested
  inner class DomainDataFunctions {

    @Test
    fun `getDomainData returns correct DomainData`() {
      val endDate = LocalDate.now().plusDays(1)
      val expectedTier = TierScore.A1
      val cas1Application = buildCas1Application()
      val cas3Application = buildCas3Application()
      val cpr = buildCorePersonRecord(sex = male)
      val cprAddresses = buildCorePersonRecordAddresses(
        addresses = listOf(
          buildAddress(
            addressStatus = AddressStatus.M,
            endDate = endDate,
          ),
        ),
      )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val orchestrationDto = EligibilityOrchestrationDto(
        crn = crn,
        cpr = cpr,
        cprAddresses = cprAddresses,
        tier = tier,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
      )

      val accommodationSummaryDto = buildAccommodationSummaryDto(endDate = endDate)

      every { accommodationQueryService.getCurrentAccommodation(crn, cprAddresses.addresses) } returns accommodationSummaryDto
      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { caseRepository.findByCrn(crn) } returns null

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tierScore).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.cas1Application).isEqualTo(cas1Application)
      assertThat(result.cas3Application).isEqualTo(cas3Application)
      assertThat(result.currentAccommodation?.endDate).isEqualTo(LocalDate.now().plusDays(1))
      assertThat(result.currentAccommodation?.isPrisonCas1Cas2OrCas2v2).isTrue()
      assertThat(result.hasNextAccommodation).isFalse()
      assertThat(result.dtrStatus).isEqualTo("submitted")
      assertThat(result.crsStatus).isEqualTo("submitted")
    }
  }

  @Nested
  inner class Cas1EligibilityScenarios {

    fun loadCas1Scenarios(): List<Cas1Scenario> {
      val rows = CsvReader().read("/cas1-eligibility-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          Cas1Scenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"]!!,
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            sex = SexCode.valueOf(row["sex"]!!),
            tierScore = TierScore.valueOf(row["tierScore"]!!),
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            cas1ApplicationStatus = row["cas1ApplicationStatus"]?.let { Cas1ApplicationStatus.valueOf(it) },
            cas1RequestForPlacementStatus = row["cas1RequestForPlacementStatus"]?.let {
              Cas1RequestForPlacementStatus.valueOf(
                it,
              )
            },
            cas1PlacementStatus = row["cas1PlacementStatus"]?.let { Cas1PlacementStatus.valueOf(it) },
            expectedCas1Status = row["expectedCas1Status"]?.let { ServiceStatus.valueOf(it) },
            expectedCas1Actions = row["expectedCas1Actions"],
            expectedCas1Link = row["expectedCas1Link"],
          )
        } catch (e: Exception) {
          throw IllegalStateException("CAS1 CSV row $idx failed: $row", e)
        }
      }
    }

    /**
     * Date formatter for CSV test data
     *
     * Uses dd/MM/yyyy format (not ISO yyyy-MM-dd) because:
     * - Excel automatically reformats ISO dates (2025-12-01) to locale format (01/12/2025)
     * - This causes test failures when the CSV is saved: "Text '01/12/2025' could not be parsed"
     * - Using formatter prevents accidentally breaking tests when modifying test cases in CSV
     *
     */
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

    @Test
    fun `should calculate eligibility for cas1 for all scenarios`() {
      val scenarios = loadCas1Scenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val cas1Application = s.cas1ApplicationStatus?.let {
          buildCas1Application(
            applicationStatus = it,
            placementStatus = s.cas1PlacementStatus,
            requestForPlacementStatus = s.cas1RequestForPlacementStatus,
          )
        }

        val currentAccommodation = s.currentAccommodationEndDate?.let {
          CurrentAccommodation(
            endDate = it,
            isPrisonCas1Cas2OrCas2v2 = false,
          )
        }

        val data = buildDomainData(
          crn = crn,
          tierScore = s.tierScore,
          sex = s.sex,
          currentAccommodation = currentAccommodation,
          cas1Application = cas1Application,
        )

        val result = eligibilityService.calculateEligibilityForCas1(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}")
          .isEqualTo(s.expectedCas1Status)

        assertThat(result.action).isEqualTo(s.expectedCas1Actions)
        assertThat(result.link).isEqualTo(s.expectedCas1Link)
      }
    }
  }

  @Nested
  inner class Cas3EligibilityScenarios {

    fun loadCas3Scenarios(): List<Cas3Scenario> {
      val rows = CsvReader().read("/cas3-eligibility-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          Cas3Scenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            currentAccommodationStatusCode = row["currentAccommodationStatusCode"]?.let {
              AccommodationArrangementType.valueOf(
                it,
              )
            },
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            cas3ApplicationStatus = row["cas3ApplicationStatus"]?.let { Cas3ApplicationStatus.valueOf(it) },
            cas3AssessmentStatus = row["cas3AssessmentStatus"]?.let { Cas3AssessmentStatus.valueOf(it) },
            cas3BookingStatus = row["cas3BookingStatus"]?.let { Cas3BookingStatus.valueOf(it) },
            crsStatus = row["crsStatus"],
            dtrStatus = row["dtrStatus"],
            expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatus.valueOf(it) },
            expectedCas3Actions = row["expectedCas3Actions"],
            expectedCas3Link = row["expectedCas3Link"],
          )
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    /**
     * Date formatter for CSV test data
     *
     * Uses dd/MM/yyyy format (not ISO yyyy-MM-dd) because:
     * - Excel automatically reformats ISO dates (2025-12-01) to locale format (01/12/2025)
     * - This causes test failures when the CSV is saved: "Text '01/12/2025' could not be parsed"
     * - Using formatter prevents accidentally breaking tests when modifying test cases in CSV
     *
     */
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

    @Test
    fun `should calculate eligibility for cas3 for all scenarios`() {
      val scenarios = loadCas3Scenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val cas3Application = s.cas3ApplicationStatus?.let {
          buildCas3Application(
            applicationStatus = it,
            bookingStatus = s.cas3BookingStatus,
            assessmentStatus = s.cas3AssessmentStatus,
          )
        }

        val currentAccommodation = s.currentAccommodationStatusCode?.let {
          CurrentAccommodation(
            endDate = s.currentAccommodationEndDate,
            isPrisonCas1Cas2OrCas2v2 = it in listOf(
              AccommodationArrangementType.PRISON,
              AccommodationArrangementType.CAS1,
              AccommodationArrangementType.CAS2,
              AccommodationArrangementType.CAS2V2,
            ),
          )
        } ?: s.currentAccommodationEndDate?.let {
          CurrentAccommodation(
            endDate = it,
            isPrisonCas1Cas2OrCas2v2 = false,
          )
        }
        val data = buildDomainData(
          crn = crn,
          tierScore = null,
          sex = null,
          currentAccommodation = currentAccommodation,
          hasNextAccommodation = s.hasNextAccommodation.toBoolean(),
          cas1Application = null,
          cas3Application = cas3Application,
          dtrStatus = s.dtrStatus,
          crsStatus = s.crsStatus,
        )

        val result = eligibilityService.calculateEligibilityForCas3(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}")
          .isEqualTo(s.expectedCas3Status)

        assertThat(result.action).isEqualTo(s.expectedCas3Actions)
        assertThat(result.link).isEqualTo(s.expectedCas3Link)
      }
    }
  }
}

data class Cas1Scenario(
  val testCaseId: String,
  val description: String,
  val referenceDate: LocalDate,
  val sex: SexCode,
  val tierScore: TierScore,
  val currentAccommodationEndDate: LocalDate?,
  val cas1ApplicationStatus: Cas1ApplicationStatus?,
  val cas1RequestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val cas1PlacementStatus: Cas1PlacementStatus?,
  val expectedCas1Status: ServiceStatus?,
  val expectedCas1Actions: String?,
  val expectedCas1Link: String?,
)

data class Cas3Scenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationStatusCode: AccommodationArrangementType?,
  val hasNextAccommodation: String,
  val currentAccommodationEndDate: LocalDate?,
  val cas3ApplicationStatus: Cas3ApplicationStatus?,
  val cas3AssessmentStatus: Cas3AssessmentStatus?,
  val cas3BookingStatus: Cas3BookingStatus?,
  val crsStatus: String?,
  val dtrStatus: String?,
  val expectedCas3Status: ServiceStatus?,
  val expectedCas3Actions: String?,
  val expectedCas3Link: String?,
)

private fun <T> runScenarios(
  scenarios: List<T>,
  run: (T) -> Unit,
) {
  val failures = mutableListOf<String>()

  scenarios.forEach { scenario ->
    try {
      run(scenario)
    } catch (ex: AssertionError) {
      failures += """
        Scenario failed:
        scenario: $scenario
        error: ${ex.message}
      """.trimIndent()
    } catch (ex: Exception) {
      failures += """
        Scenario errored:
        scenario: $scenario
        error: ${ex::class.simpleName}: ${ex.message}
      """.trimIndent()
    }
  }

  if (failures.isNotEmpty()) {
    fail(
      """
      Scenario failures: ${failures.size}

      ${failures.joinToString("\n\n")}
      """.trimIndent(),
    )
  }
}
