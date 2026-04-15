package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.completion.Cas1ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.eligibility.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.suitability.Cas1ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.completion.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.CrsStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.CurrentAddressTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.NoConflictingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.suitability.Cas3ApplicationExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.Cas3RecentReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.ReleaseDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.TestData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(value = [MockKExtension::class])
class EligibilityServiceTest {

  private val clock = MutableClock()
  private val crn = "ABC1234"
  private val male = buildSex(SexCode.M)

  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()
  private val caseRepository = mockk<CaseRepository>()

  var cas1EligibilityRuleSet = Cas1EligibilityRuleSet(
    listOf(MaleRiskEligibilityRule(), NonMaleRiskEligibilityRule(), STierEligibilityRule()),
  )
  var cas3ValidationRuleSet = Cas3ValidationRuleSet(listOf(ReleaseDateValidationRule()))
  var cas1ValidationRuleSet = Cas1ValidationRuleSet(listOf(ReleaseDateValidationRule(), Cas1SexValidationRule()))
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(listOf(Cas1ApplicationSuitabilityRule()))
  var cas1ContextUpdater = Cas1ContextUpdater(clock)
  var commonContextUpdater = CommonContextUpdater()
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(listOf(Cas1ApplicationCompletionRule()))

  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    listOf(
      CurrentAddressTypeRule(),
      NextAccommodationRule(),
      DtrStatusRule(),
      CrsStatusRule(),
      NoConflictingCas1BookingRule(),
    ),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(listOf(Cas3ApplicationCompletionRule(), Cas3RecentReleaseDateRule(clock)))
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(listOf(Cas3ApplicationSuitabilityRule(), Cas3RecentReleaseDateRule(clock), Cas3ApplicationExpiredRule()))

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
  )

  @Nested
  inner class DomainDataFunctions {

    @Test
    fun `buildDomainData maps all fields correctly`() {
      val cpr = CorePersonRecord(sex = Sex(SexCode.M, "Male"), identifiers = null)
      val tier = Tier(TierScore.A1, UUID.randomUUID(), LocalDateTime.now(), null)
      val releaseDate = LocalDate.now().plusYears(1)
      val prisoner = listOf(
        Prisoner(releaseDate = LocalDate.now().plusMonths(5)),
        Prisoner(releaseDate = releaseDate),
        Prisoner(releaseDate = LocalDate.now().plusMonths(4)),
        Prisoner(releaseDate = null),
      )

      val result = DomainData(crn, cpr, tier, prisoner, null, null, null, null)
      assertThat(result.tierScore).isEqualTo(tier.tierScore)
      assertThat(result.sex).isEqualTo(cpr.sex?.code)
      assertThat(result.releaseDate).isEqualTo(releaseDate)
    }

    @Test
    fun `getDomainData returns correct DomainData`() {
      val expectedTier = TierScore.A1
      val expectedReleaseDate = LocalDate.now().plusYears(1)

      val prisonerNumber = "PN1"
      val cpr = CorePersonRecord(sex = male, identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber)))
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val prisoner = Prisoner(releaseDate = expectedReleaseDate)
      val orchestrationDto = EligibilityOrchestrationDto(crn, cpr, tier, null, null)

      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { eligibilityOrchestrationService.getPrisonerData(listOf(prisonerNumber)) } returns listOf(prisoner)
      every { caseRepository.findByCrn(crn) } returns null

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tierScore).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDate)
    }
  }
}

@ExtendWith(value = [MockKExtension::class])
class ConcurrentEligibilityServiceTest {

  private val clock = MutableClock()
  private val crn = "ABC1234"

  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()

  var cas1EligibilityRuleSet = Cas1EligibilityRuleSet(
    listOf(MaleRiskEligibilityRule(), NonMaleRiskEligibilityRule(), STierEligibilityRule()),
  )
  var cas3ValidationRuleSet = Cas3ValidationRuleSet(listOf(ReleaseDateValidationRule()))
  var cas1ValidationRuleSet = Cas1ValidationRuleSet(listOf(ReleaseDateValidationRule(), Cas1SexValidationRule()))
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(listOf(Cas1ApplicationSuitabilityRule()))
  var cas1ContextUpdater = Cas1ContextUpdater(clock)
  var commonContextUpdater = CommonContextUpdater()
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(listOf(Cas1ApplicationCompletionRule()))

  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    listOf(
      CurrentAddressTypeRule(),
      NextAccommodationRule(),
      DtrStatusRule(),
      CrsStatusRule(),
      NoConflictingCas1BookingRule(),
    ),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(listOf(Cas3ApplicationCompletionRule(), Cas3RecentReleaseDateRule(clock)))
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(listOf(Cas3ApplicationSuitabilityRule(), Cas3RecentReleaseDateRule(clock), Cas3ApplicationExpiredRule()))

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
  )

  @Execution(ExecutionMode.CONCURRENT)
  @Nested
  inner class Cas1EligibilityScenarios {

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

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = ["/cas1-eligibility-scenarios.csv"], numLinesToSkip = 1, nullValues = ["None"])
    @TestData
    fun `should calculate eligibility for cas1 for all scenarios`(
      testCaseId: String,
      description: String,
      referenceDate: String,
      sex: SexCode,
      tierScore: TierScore,
      releaseDate: String?,
      cas1Status: Cas1ApplicationStatus?,
      cas1RequestForPlacementStatus: Cas1RequestForPlacementStatus?,
      cas1PlacementStatus: Cas1PlacementStatus?,
      expectedCas1Status: ServiceStatus?,
      expectedCas1ActionsString: String?,
      expectedCas1Link: String?,
    ) {
      clock.setNow(referenceDate.toLocalDate())

      val cas1Application = cas1Status?.let {
        buildCas1Application(applicationStatus = it, placementStatus = cas1PlacementStatus, requestForPlacementStatus = cas1RequestForPlacementStatus)
      }
      val data = buildDomainData(crn, tierScore, sex, releaseDate?.toLocalDate(), cas1Application = cas1Application)

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(expectedCas1Status)
      assertThat(result.action).isEqualTo(expectedCas1ActionsString)
      assertThat(result.link).isEqualTo(expectedCas1Link)
    }
  }

  @Execution(ExecutionMode.CONCURRENT)
  @Nested
  inner class Cas3EligibilityScenarios {

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

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = ["/cas3-eligibility-scenarios.csv"], numLinesToSkip = 1, nullValues = ["None"])
    @TestData
    fun `should calculate eligibility for cas3 for all scenarios`(
      testCaseId: String,
      description: String?,
      referenceDate: String,
      currentAccommodationArrangementType: AccommodationArrangementType?,
      hasNextAccommodation: String,
      releaseDate: String?,
      cas3Status: Cas3ApplicationStatus?,
      cas3AssessmentStatus: Cas3AssessmentStatus?,
      cas3BookingStatus: Cas3BookingStatus?,
      crsStatus: String?,
      dtrStatus: String?,
      expectedCas3Status: ServiceStatus?,
      expectedCas3ActionsString: String?,
      expectedCas3Link: String?,
    ) {
      clock.setNow(referenceDate.toLocalDate())

      val cas3Application = cas3Status?.let {
        buildCas3Application(applicationStatus = it, bookingStatus = cas3BookingStatus, assessmentStatus = cas3AssessmentStatus)
      }

      val data = buildDomainData(
        crn = crn,
        tierScore = null,
        sex = null,
        releaseDate = releaseDate?.toLocalDate(),
        currentAccommodationArrangementType = currentAccommodationArrangementType,
        hasNextAccommodation = hasNextAccommodation.toBoolean(),
        cas1Application = null,
        cas3Application = cas3Application,
        dtrStatus = dtrStatus,
        crsStatus = crsStatus,
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(expectedCas3Status)
      assertThat(result.action).isEqualTo(expectedCas3ActionsString)
      assertThat(result.link).isEqualTo(expectedCas3Link)
    }
  }
}
