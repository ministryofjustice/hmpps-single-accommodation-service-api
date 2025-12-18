package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
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
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(MockKExtension::class)
class EligibilityServiceTest : EligibilityBaseTest() {

  @MockK
  lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @InjectMockKs
  lateinit var eligibilityService: EligibilityService

  private val crn = "ABC1234"

  @Nested
  inner class DomainDataFunctions {
    private fun transformPrisonerReleaseDate(date: LocalDate?): OffsetDateTime? = date?.atStartOfDay()?.atOffset(ZoneOffset.UTC)

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

  @ParameterizedTest
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
    val fixedClock = createFixedClock(referenceDate)
    val testEligibilityService = createTestEligibilityService(fixedClock)

    val cas1Application = cas1Status?.let {
      buildCas1Application(
        applicationStatus = cas1Status,
        placementStatus = cas1PlacementStatus.takeIf { it != null },
      )
    }

    val releaseDate = LocalDate.parse(releaseDate, dateFormatter)
      .atStartOfDay()
      .atOffset(ZoneOffset.UTC)
    val data = buildDomainData(crn, tier = tier, buildSex(sex), releaseDate, cas1Application)

    val result = testEligibilityService.calculateEligibilityForCas1(data)

    val actualActions = if (result.actions.isEmpty()) null else result.actions.joinToString(",")
    assertThat(result.serviceStatus).isEqualTo(expectedCas1Status)
    assertThat(actualActions).isEqualTo(expectedCas1Actions)
  }

  private fun createFixedClock(referenceDate: String): Clock {
    val parsedDate = LocalDate.parse(referenceDate, dateFormatter)
    return Clock.fixed(
      parsedDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
      ZoneOffset.UTC,
    )
  }

  private fun createTestEligibilityService(clock: Clock) = EligibilityService(
    eligibilityOrchestrationService,
    Cas1RuleSet(sTierRule, maleRiskRule, nonMaleRiskRule, WithinSixMonthsOfReleaseRule(clock)),
    defaultRulesEngine,
  )
}
