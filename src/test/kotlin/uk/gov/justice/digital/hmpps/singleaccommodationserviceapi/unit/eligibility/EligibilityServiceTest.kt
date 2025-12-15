package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class EligibilityServiceTest : EligibilityBaseTest() {
  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()

  var eligibilityService = EligibilityService(
    eligibilityOrchestrationService,
    cas1RuleSet,
    defaultRulesEngine,
  )

  private val crn = "ABC1234"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityServiceTest#provideDataAndEligibility")
  fun `should calculate eligibility for cas1`(
    data: DomainData,
    serviceResult: ServiceResult,
  ) {
    val result = eligibilityService.calculateEligibilityForCas1(data)
    assertThat(
      result,
    ).isEqualTo(serviceResult)
  }

  @Nested
  inner class DomainDataFunctions {
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

  private companion object {

    private const val CRN = "ABC1234"
    private const val CREATE_PLACEMENT_ACTION = "Create a placement request."

    private val male = Sex(
      code = SexCode.M,
      description = "Male",
    )

    // TODO add test case scenarios to here
    @JvmStatic
    fun provideDataAndEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1,
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = Cas1PlacementStatus.DEPARTED,
          ),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.DEPARTED,
          actions = listOf(CREATE_PLACEMENT_ACTION),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1,
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
            placementStatus = null,
          ),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.ASSESSMENT_IN_PROGRESS,
          actions = listOf(CREATE_PLACEMENT_ACTION),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1,
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = Cas1PlacementStatus.UPCOMING,
          ),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.UPCOMING_PLACEMENT,
          actions = listOf(),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1,
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.UPCOMING,
          actions = listOf("Start approved premise referral in 31 days"),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1,
          sex = male,
          releaseDate = OffsetDateTime.now(),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          actions = listOf("Start approved premise referral"),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = TierScore.A1S,
          sex = male,
          releaseDate = OffsetDateTime.now(),
        ),
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          actions = listOf(),
        ),
      ),
    )
  }
}
