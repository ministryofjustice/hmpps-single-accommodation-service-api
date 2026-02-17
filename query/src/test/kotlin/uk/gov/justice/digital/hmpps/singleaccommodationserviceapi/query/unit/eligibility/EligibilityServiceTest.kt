package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ReleaseDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2CourtBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2HdcRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2PrisonBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CrsStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CurrentAddressTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NoConflictingCas1BookingRule
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

  var cas1ValidationRuleSet = Cas1ValidationRuleSet(listOf(ReleaseDateValidationRule()))
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(listOf(ApplicationSuitabilityRule()))
  var cas1ContextUpdater = Cas1ContextUpdater(clock)
  var cas1ValidationContextUpdater = Cas1ValidationContextUpdater()
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(listOf(ApplicationCompletionRule()))
  var cas2HdcRules = Cas2HdcRuleSet(emptyList())
  var cas2CourtBailRules = Cas2CourtBailRuleSet(emptyList())
  var cas2PrisonBailRules = Cas2PrisonBailRuleSet(emptyList())

  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    listOf(
      CurrentAddressTypeRule(),
      NextAccommodationRule(),
      DtrStatusRule(),
      CrsStatusRule(),
      NoConflictingCas1BookingRule(),
    ),
  )
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(listOf(Cas3ApplicationSuitabilityRule()))
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(listOf(Cas3ApplicationCompletionRule()))
  var cas3ContextUpdater = Cas3ContextUpdater(clock)

  var rulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  private val eligibilityService = EligibilityService(
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    caseRepository = caseRepository,
    cas1EligibilityRuleSet = cas1EligibilityRuleSet,
    cas1SuitabilityRuleSet = cas1SuitabilityRuleSet,
    cas1CompletionRuleSet = cas1CompletionRuleSet,
    cas1ValidationRuleSet = cas1ValidationRuleSet,
    cas2HdcRuleSet = cas2HdcRules,
    cas2PrisonBailRuleSet = cas2PrisonBailRules,
    cas1ContextUpdater = cas1ContextUpdater,
    cas1ValidationContextUpdater = cas1ValidationContextUpdater,
    cas2CourtBailRuleSet = cas2CourtBailRules,
    cas3EligibilityRuleSet = cas3EligibilityRuleSet,
    cas3SuitabilityRuleSet = cas3SuitabilityRuleSet,
    cas3CompletionRuleSet = cas3CompletionRuleSet,
    cas3ContextUpdater = cas3ContextUpdater,
    engine = rulesEngine,
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

      val result = DomainData(crn, cpr, tier, prisoner, null, null, null, null, null, null)
      assertThat(result.tier).isEqualTo(tier.tierScore)
      assertThat(result.sex).isEqualTo(cpr.sex?.code)
      assertThat(result.releaseDate).isEqualTo(releaseDate)
    }

    @Test
    fun `getDomainData returns correct DomainData when no case in DB - uses DTO tier`() {
      val expectedTier = TierScore.A1
      val expectedReleaseDate = LocalDate.now().plusYears(1)

      val crn = "X12345"
      val prisonerNumber = "PN1"
      val cpr =
        CorePersonRecord(
          sex = male,
          identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber))
        )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val prisoner = Prisoner(releaseDate = expectedReleaseDate)
      val orchestrationDto = EligibilityOrchestrationDto(crn, cpr, tier, null, null, null, null)

      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { eligibilityOrchestrationService.getPrisonerData(listOf(prisonerNumber)) } returns
        listOf(prisoner)
      every { caseRepository.findTierScoreByCrn(crn) } returns null

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tier).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDate)
    }

    @Test
    fun `getDomainData uses tier from CaseRepository when case exists - not DTO tier`() {
      val dtoTier = TierScore.A1
      val dbTier = TierScore.B2
      val expectedReleaseDate = LocalDate.now().plusYears(1)

      val crn = "X12345"
      val prisonerNumber = "PN1"
      val cpr =
        CorePersonRecord(
          sex = male,
          identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber))
        )
      val orchestrationDto =
        EligibilityOrchestrationDto(
          crn,
          cpr,
          Tier(dtoTier, UUID.randomUUID(), LocalDateTime.now(), null),
          null,
          null,
          null,
          null,
        )
      val prisoner = Prisoner(releaseDate = expectedReleaseDate)

      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { eligibilityOrchestrationService.getPrisonerData(listOf(prisonerNumber)) } returns
        listOf(prisoner)
      every { caseRepository.findTierScoreByCrn(crn) } returns dbTier

      val result = eligibilityService.getDomainData(crn)

      assertThat(result.tier).isEqualTo(dbTier)
      assertThat(result.tier).isNotEqualTo(dtoTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDate)
    }
  }

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
      description: String,
      referenceDate: String,
      sex: SexCode,
      tier: TierScore,
      releaseDate: String?,
      cas1Status: Cas1ApplicationStatus?,
      cas1PlacementStatus: Cas1PlacementStatus?,
      expectedCas1Status: ServiceStatus?,
      expectedCas1ActionsString: String?,
      cas1ActionsAreUpcoming: Boolean,
    ) {
      clock.setNow(referenceDate.toLocalDate())

      val cas1Application = cas1Status?.let {
        buildCas1Application(applicationStatus = it, placementStatus = cas1PlacementStatus)
      }
      val data = buildDomainData(crn, tier, sex, releaseDate?.toLocalDate(), cas1Application = cas1Application)

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(expectedCas1Status)
      val expectedActions = expectedCas1ActionsString?.let {
        RuleAction(it, cas1ActionsAreUpcoming)
      }
      assertThat(result.action).isEqualTo(expectedActions)
    }
  }
}
