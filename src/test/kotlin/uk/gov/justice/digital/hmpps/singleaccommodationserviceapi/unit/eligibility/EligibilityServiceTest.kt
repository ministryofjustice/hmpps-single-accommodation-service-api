package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.MutableClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(MockKExtension::class)
class EligibilityServiceTest : EligibilityBaseTest() {
  @MockK
  private lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

  private val crn = "ABC1234"

  @Nested
  inner class DomainDataFunctions {
    private val eligibilityService = EligibilityService(
      eligibilityOrchestrationService,
      cas1RuleSet,
      defaultRulesEngine,
    )

    private fun transformPrisonerReleaseDate(date: LocalDate?): OffsetDateTime? = date?.atStartOfDay()?.atOffset(java.time.ZoneOffset.UTC)

    @Test
    fun `buildDomainData maps all fields correctly`() {
      val cpr = CorePersonRecord(sex = Sex(SexCode.M, "Male"), identifiers = null)
      val tier = Tier(TierScore.A1, UUID.randomUUID(), LocalDateTime.now(), null)
      val releaseDate = LocalDate.now().plusMonths(6)
      val prisoner = listOf(
        Prisoner(releaseDate = LocalDate.now().plusMonths(5)),
        Prisoner(releaseDate = releaseDate),
        Prisoner(releaseDate = LocalDate.now().plusMonths(4)),
        Prisoner(releaseDate = null),
      )
      val expectedReleaseDateOffset = transformPrisonerReleaseDate(releaseDate)

      val result = DomainData(crn, cpr, tier, prisoner, null)
      assertThat(result.tier).isEqualTo(tier.tierScore)
      assertThat(result.sex).isEqualTo(Sex(cpr.sex?.code, cpr.sex?.description))
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDateOffset)
    }

    @Test
    fun `getDomainData returns correct DomainData`() {
      val expectedTier = TierScore.A1
      val expectedReleaseDate = LocalDate.now().plusMonths(6)
      val expectedReleaseDateOffset = transformPrisonerReleaseDate(expectedReleaseDate)

      val crn = "X12345"
      val prisonerNumber = "PN1"
      val cpr = CorePersonRecord(sex = male, identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber)))
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val prisoner = Prisoner(releaseDate = expectedReleaseDate)
      val orchestrationDto = EligibilityOrchestrationDto(crn, cpr, tier, null)

      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { eligibilityOrchestrationService.getPrisonerData(listOf(prisonerNumber)) } returns listOf(prisoner)

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tier).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(male)
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDateOffset)
    }
  }

  @Nested
  inner class Cas1EligibilityScenarios {
    private val clock = MutableClock()
    private val eligibilityServiceWithClock = EligibilityService(
      eligibilityOrchestrationService,
      Cas1RuleSet(sTierRule, maleRiskRule, nonMaleRiskRule, WithinSixMonthsOfReleaseRule(clock)),
      defaultRulesEngine,
    )

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
    private fun String.toOffsetDateTime(): OffsetDateTime = toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC)

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = ["/cas1-eligibility-scenarios.csv"], numLinesToSkip = 1, nullValues = ["None"])
    @Suppress("LongParameterList")
    fun `should calculate eligibility for cas1 for all scenarios`(
      description: String,
      referenceDate: String,
      sex: SexCode,
      tier: TierScore,
      releaseDate: String,
      cas1Status: Cas1ApplicationStatus?,
      cas1PlacementStatus: Cas1PlacementStatus?,
      expectedCas1Status: ServiceStatus?,
      expectedCas1Actions: String?,
    ) {
      clock.setNow(referenceDate.toLocalDate())

      val cas1Application = cas1Status?.let {
        buildCas1Application(applicationStatus = it, placementStatus = cas1PlacementStatus)
      }
      val data = buildDomainData(crn, tier, buildSex(sex), releaseDate.toOffsetDateTime(), cas1Application)

      val result = eligibilityServiceWithClock.calculateEligibilityForCas1(data)

      val actualActions = result.actions.takeIf { it.isNotEmpty() }?.joinToString(",")
      assertThat(result.serviceStatus).isEqualTo(expectedCas1Status)
      assertThat(actualActions).isEqualTo(expectedCas1Actions)
    }
  }
}
