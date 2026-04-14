package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NoConflictingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationPresentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3BookingSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrApplicationCompleteRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.CurrentAddressTypeNotPrivateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrRecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
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

  var rulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  private val accommodationQueryService = mockk<AccommodationQueryService>()
  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()
  private val dutyToReferQueryService = mockk<DutyToReferQueryService>()

  private val caseRepository = mockk<CaseRepository>()

  // CAS1
  var cas1ContextUpdater = Cas1ContextUpdater(clock)
  var cas1ValidationRuleSet = Cas1ValidationRuleSet(
    listOf(
      CurrentAccommodationEndDateValidationRule(),
      Cas1SexValidationRule(),
    ),
  )
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(listOf(Cas1ApplicationCompletionRule()))
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(listOf(Cas1ApplicationSuitabilityRule()))
  var cas1EligibilityRuleSet = Cas1EligibilityRuleSet(
    listOf(
      MaleRiskEligibilityRule(),
      NonMaleRiskEligibilityRule(),
      STierEligibilityRule(),
    ),
  )

  // CAS3
  var cas3ContextUpdater = Cas3ContextUpdater(clock)
  var cas3ValidationRuleSet = Cas3ValidationRuleSet(listOf(CurrentAccommodationEndDateValidationRule()))
  val cas3UpcomingContextUpdater = Cas3UpcomingContextUpdater(clock)
  var cas3UpcomingRuleSet = Cas3UpcomingRuleSet(listOf(Cas3RecentCurrentAccommodationEndDateRule(clock)))
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(
    listOf(
      Cas3ApplicationSuitabilityRule(),
      Cas3ApplicationPresentSuitabilityRule(),
      Cas3BookingSuitabilityRule(),
      Cas3AssessmentSuitabilityRule(),
    ),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(listOf(Cas3ApplicationCompletionRule()))
  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    listOf(
      CurrentAccommodationTypeRule(),
      NextAccommodationRule(),
      DtrExpiredReferralRule(clock),
      CrsStatusRule(),
      NoConflictingCas1BookingRule(),
    ),
  )

  // DTR
  val dtrUpcomingContextUpdater = DtrUpcomingContextUpdater()
  var dtrUpcomingRuleSet = DtrUpcomingRuleSet(listOf(DtrRecentCurrentAccommodationEndDateRule(clock)))
  var dtrSuitabilityContextUpdater = DtrSuitabilityContextUpdater()
  var dtrSuitabilityRuleSet = DtrSuitabilityRuleSet(
    listOf(
      DtrStatusRule(),
      DtrPresentRule(),
      DtrExpiredReferralRule(clock),
    ),
  )
  var dtrCompletionContextUpdater = DtrCompletionContextUpdater()
  var dtrCompletionRuleSet = DtrCompletionRuleSet(listOf(DtrApplicationCompleteRule()))
  var dtrEligibilityRuleSet = DtrEligibilityRuleSet(
    listOf(
      CurrentAddressTypeNotPrivateRule(),
      NextAccommodationRule(),
    ),
  )

  // COMMON
  var commonContextUpdater = CommonContextUpdater()

  private val eligibilityService = EligibilityService(
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    cas1EligibilityRuleSet = cas1EligibilityRuleSet,
    cas1SuitabilityRuleSet = cas1SuitabilityRuleSet,
    cas1CompletionRuleSet = cas1CompletionRuleSet,
    cas1ValidationRuleSet = cas1ValidationRuleSet,
    cas1ContextUpdater = cas1ContextUpdater,
    commonContextUpdater = commonContextUpdater,
    cas3EligibilityRuleSet = cas3EligibilityRuleSet,
    cas3SuitabilityRuleSet = cas3SuitabilityRuleSet,
    cas3CompletionRuleSet = cas3CompletionRuleSet,
    cas3ContextUpdater = cas3ContextUpdater,
    engine = rulesEngine,
    cas3ValidationRuleSet = cas3ValidationRuleSet,
    dtrCompletionRuleSet = dtrCompletionRuleSet,
    dutyToReferQueryService = dutyToReferQueryService,
    dtrSuitabilityContextUpdater = dtrSuitabilityContextUpdater,
    dtrEligibilityRuleSet = dtrEligibilityRuleSet,
    dtrSuitabilityRuleSet = dtrSuitabilityRuleSet,
    dtrUpcomingContextUpdater = dtrUpcomingContextUpdater,
    dtrUpcomingRuleSet = dtrUpcomingRuleSet,
    dtrCompletionContextUpdater = dtrCompletionContextUpdater,
    caseRepository = caseRepository,
    accommodationQueryService = accommodationQueryService,
    cas3UpcomingRuleSet = cas3UpcomingRuleSet,
    cas3UpcomingContextUpdater = cas3UpcomingContextUpdater,
  )

  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  private fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

  @Nested
  inner class DomainDataFunctions {
    val crn = "ABC123"

    @Test
    fun `getDomainData returns correct DomainData`() {
      val endDate = LocalDate.now().plusDays(1)
      val expectedTier = TierScore.A1
      val caseId = UUID.randomUUID()
      val cas1Application = buildCas1Application()
      val cas3Application = buildCas3Application()
      val cpr = buildCorePersonRecord(sex = buildSex(SexCode.M))
      val cprAddresses = buildCorePersonRecordAddresses(
        addresses = listOf(
          buildAddress(
            addressStatus = AddressStatus.M,
            endDate = endDate,
          ),
        ),
      )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val dutyToRefer = buildDutyToReferDto(crn, UUID.randomUUID(), DtrStatus.SUBMITTED, null)
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = cpr,
          cprAddresses = cprAddresses,
          tier = tier,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
        ),
      )
      val accommodationSummaryDto = buildAccommodationSummaryDto(endDate = endDate)
      val caseEntity = buildCaseEntity(id = caseId)
      val currentAccommodation = buildCurrentAccommodation(endDate = endDate, isPrivate = false, isPrisonCas1Cas2OrCas2v2 = true)

      every { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) } returns dutyToRefer
      every { accommodationQueryService.getCurrentAccommodation(crn, cprAddresses.addresses) } returns accommodationSummaryDto
      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { caseRepository.findByCrn(crn) } returns caseEntity

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tierScore).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.cas1Application).isEqualTo(cas1Application)
      assertThat(result.cas3Application).isEqualTo(cas3Application)
      assertThat(result.currentAccommodation).isEqualTo(currentAccommodation)
      assertThat(result.hasNextAccommodation).isFalse()
      assertThat(result.crsStatus).isEqualTo("SUBMITTED")
      assertThat(result.dutyToRefer).isEqualTo(dutyToRefer)
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
          crn = s.testCaseId,
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
  inner class DtrEligibilityScenarios {

    fun loadDtrScenarios(): List<DtrScenario> {
      val rows = CsvReader().read("/dtr-eligibility-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          DtrScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            isPrivateCurrentAccommodation = row["isPrivateCurrentAccommodation"]!!,
            dtrStatus = row["dtrStatus"]?.let { DtrStatus.valueOf(it) },
            dtrSubmissionDate = row["dtrSubmissionDate"]?.toLocalDate(),
            expectedDtrStatus = row["expectedDtrStatus"]?.let { ServiceStatus.valueOf(it) },
            expectedDtrActions = row["expectedDtrActions"],
            expectedDtrLink = row["expectedDtrLink"],
          )
        } catch (e: Exception) {
          throw IllegalStateException("DTR CSV row $idx failed: $row", e)
        }
      }
    }

    @Test
    fun `should calculate eligibility for dtr for all scenarios`() {
      val scenarios = loadDtrScenarios()

      runScenarios(scenarios) { s ->
        clock.setNow(s.referenceDate)

        val dutyToRefer = s.dtrStatus?.let { it ->
          buildDutyToReferDto(
            status = it,
            submission = s.dtrSubmissionDate?.let {
              buildDtrSubmission(submissionDate = it)
            },
          )
        }

        val currentAccommodation = s.currentAccommodationEndDate?.let {
          buildCurrentAccommodation(
            endDate = it,
            isPrivate = s.isPrivateCurrentAccommodation.toBoolean(),
          )
        } ?: buildCurrentAccommodation(endDate = null, isPrivate = s.isPrivateCurrentAccommodation.toBoolean())

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          dutyToRefer = dutyToRefer,
          hasNextAccommodation = s.hasNextAccommodation.toBoolean(),
          currentAccommodation = currentAccommodation,
        )

        val result = eligibilityService.calculateEligibilityForDtr(data)

        assertThat(result.serviceStatus).isEqualTo(s.expectedDtrStatus)
        assertThat(result.action).isEqualTo(s.expectedDtrActions)
        assertThat(result.link).isEqualTo(s.expectedDtrLink)
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
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            hasCas1UpcomingBooking = row["hasCas1UpcomingBooking"]!!,
            isPrisonCas1Cas2OrCas2v2CurrentAccommodation = row["isPrisonCas1Cas2OrCas2v2CurrentAccommodation"]!!,
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            dtrSubmissionDate = row["dtrSubmissionDate"]?.toLocalDate(),
            cas3ApplicationStatus = row["cas3ApplicationStatus"]?.let { Cas3ApplicationStatus.valueOf(it) },
            cas3AssessmentStatus = row["cas3AssessmentStatus"]?.let { Cas3AssessmentStatus.valueOf(it) },
            cas3BookingStatus = row["cas3BookingStatus"]?.let { Cas3BookingStatus.valueOf(it) },
            expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatus.valueOf(it) },
            expectedCas3Actions = row["expectedCas3Actions"],
            expectedCas3Link = row["expectedCas3Link"],
          )
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    @Test
    fun `should calculate eligibility for cas3 for all scenarios`() {
      val scenarios = loadCas3Scenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val dutyToRefer = s.dtrSubmissionDate?.let {
          buildDutyToReferDto(
            submission = buildDtrSubmission(submissionDate = it),
          )
        }

        val cas1Application = if (s.hasCas1UpcomingBooking.toBoolean()) {
          buildCas1Application(
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
            placementStatus = Cas1PlacementStatus.UPCOMING,
          )
        } else {
          null
        }

        val cas3Application = s.cas3ApplicationStatus?.let {
          buildCas3Application(
            applicationStatus = it,
            bookingStatus = s.cas3BookingStatus,
            assessmentStatus = s.cas3AssessmentStatus,
          )
        }

        val currentAccommodation = buildCurrentAccommodation(
          endDate = s.currentAccommodationEndDate,
          isPrisonCas1Cas2OrCas2v2 = s.isPrisonCas1Cas2OrCas2v2CurrentAccommodation.toBoolean(),
        )
        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          currentAccommodation = currentAccommodation,
          hasNextAccommodation = s.hasNextAccommodation.toBoolean(),
          cas1Application = cas1Application,
          cas3Application = cas3Application,
          dutyToRefer = dutyToRefer,
        )

        val result = eligibilityService.calculateEligibilityForCas3(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas3Status}")
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

data class DtrScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationEndDate: LocalDate?,
  val hasNextAccommodation: String,
  val isPrivateCurrentAccommodation: String,
  val dtrStatus: DtrStatus?,
  val dtrSubmissionDate: LocalDate?,
  val expectedDtrStatus: ServiceStatus?,
  val expectedDtrActions: String?,
  val expectedDtrLink: String?,
)

data class Cas3Scenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val hasNextAccommodation: String,
  val hasCas1UpcomingBooking: String,
  val isPrisonCas1Cas2OrCas2v2CurrentAccommodation: String,
  val currentAccommodationEndDate: LocalDate?,
  val dtrSubmissionDate: LocalDate?,
  val cas3ApplicationStatus: Cas3ApplicationStatus?,
  val cas3AssessmentStatus: Cas3AssessmentStatus?,
  val cas3BookingStatus: Cas3BookingStatus?,
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
