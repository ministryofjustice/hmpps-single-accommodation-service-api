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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
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
}

@ExtendWith(value = [MockKExtension::class])
class ConcurrentEligibilityServiceTest {

  private val clock = MutableClock()
  private val crn = "ABC1234"

  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()
  private val accommodationQueryService = mockk<AccommodationQueryService>()

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
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(listOf(Cas3ApplicationSuitabilityRule(), Cas3RecentCurrentAccommodationEndDateRule(clock), Cas3ApplicationExpiredRule()))

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
      currentAccommodationEndDate: String?,
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

      val currentAccommodation = currentAccommodationEndDate?.let {
        CurrentAccommodation(
          endDate = it.toLocalDate(),
          isPrisonCas1Cas2OrCas2v2 = false,
        )
      }

      val data = buildDomainData(crn, tierScore, sex, currentAccommodation, cas1Application = cas1Application)

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
      currentAccommodationType: AccommodationArrangementType?,
      hasNextAccommodation: String,
      currentAccommodationEndDate: String?,
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

      val currentAccommodation = currentAccommodationType?.let {
        CurrentAccommodation(
          endDate = currentAccommodationEndDate?.toLocalDate(),
          isPrisonCas1Cas2OrCas2v2 = when (currentAccommodationType) {
            AccommodationArrangementType.PRISON,
            AccommodationArrangementType.CAS1,
            AccommodationArrangementType.CAS2,
            AccommodationArrangementType.CAS2V2,
            -> true

            else -> false
          },
        )
      } ?: currentAccommodationEndDate?.let {
        CurrentAccommodation(
          endDate = it.toLocalDate(),
          isPrisonCas1Cas2OrCas2v2 = false,
        )
      }

      val data = buildDomainData(
        crn = crn,
        tierScore = null,
        sex = null,
        currentAccommodation = currentAccommodation,
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
