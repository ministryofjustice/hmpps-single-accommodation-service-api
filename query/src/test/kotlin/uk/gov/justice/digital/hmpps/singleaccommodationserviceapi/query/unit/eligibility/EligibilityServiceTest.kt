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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.STierEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NoConflictingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationPresentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3BookingSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.IsMaleRule
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas1ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas3ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.PaEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCommissionedRehabilitativeServices
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
  var cas1CompletionContextUpdater = Cas1CompletionContextUpdater()
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
  val cas1UpcomingContextUpdater = Cas1UpcomingContextUpdater(clock)
  var cas1UpcomingRuleSet = Cas1UpcomingRuleSet(listOf(RecentCurrentAccommodationEndDateRule(clock)))
  val cas1SuitabilityContextUpdater = Cas1SuitabilityContextUpdater()

  // CAS3
  var cas3SuitabilityContextUpdater = Cas3SuitabilityContextUpdater()
  var cas3CompletionContextUpdater = Cas3CompletionContextUpdater()
  var cas3ValidationRuleSet = Cas3ValidationRuleSet(listOf(CurrentAccommodationEndDateValidationRule()))
  val cas3UpcomingContextUpdater = Cas3UpcomingContextUpdater(clock)
  var cas3UpcomingRuleSet = Cas3UpcomingRuleSet(listOf(RecentCurrentAccommodationEndDateRule(clock)))
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
      NoNextAccommodationRule(),
      DtrExpiredReferralRule(clock),
      NoConflictingCas1BookingRule(),
      CrsSubmittedRule(),
      CrsExpiredRule(clock),
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
      NoNextAccommodationRule(),
    ),
  )

  // CRS
  var crsEligibilityRuleSet = CrsEligibilityRuleSet(
    listOf(IsMaleRule(), CurrentAccommodationEndDateValidationRule(), NoNextAccommodationRule()),
  )
  var crsCompletionRuleSet = CrsCompletionRuleSet(
    listOf(CrsSubmittedRule(), CrsExpiredRule(clock)),
  )
  var crsContextUpdater = CrsContextUpdater()

  // COMMON
  var commonContextUpdater = CommonContextUpdater()

  // PA
  var paEligibilityRuleSet = PaEligibilityRuleSet(
    listOf(Cas1ApplicationNotSuitableRule(), Cas3ApplicationNotSuitableRule()),
  )
  var paCompletionRuleSet = PaCompletionRuleSet(
    listOf(HasNextAccommodationRule()),
  )
  var paCompletionContextUpdater = PaCompletionContextUpdater()

  private val eligibilityService = EligibilityService(
    accommodationQueryService = accommodationQueryService,
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    cas1EligibilityRuleSet = cas1EligibilityRuleSet,
    cas1SuitabilityRuleSet = cas1SuitabilityRuleSet,
    cas1CompletionRuleSet = cas1CompletionRuleSet,
    cas1ValidationRuleSet = cas1ValidationRuleSet,
    cas3ValidationRuleSet = cas3ValidationRuleSet,
    cas1CompletionContextUpdater = cas1CompletionContextUpdater,
    commonContextUpdater = commonContextUpdater,
    cas3EligibilityRuleSet = cas3EligibilityRuleSet,
    cas3SuitabilityRuleSet = cas3SuitabilityRuleSet,
    cas3CompletionRuleSet = cas3CompletionRuleSet,
    cas3CompletionContextUpdater = cas3CompletionContextUpdater,
    engine = rulesEngine,
    dtrCompletionRuleSet = dtrCompletionRuleSet,
    dutyToReferQueryService = dutyToReferQueryService,
    dtrSuitabilityContextUpdater = dtrSuitabilityContextUpdater,
    dtrEligibilityRuleSet = dtrEligibilityRuleSet,
    dtrSuitabilityRuleSet = dtrSuitabilityRuleSet,
    dtrUpcomingContextUpdater = dtrUpcomingContextUpdater,
    dtrUpcomingRuleSet = dtrUpcomingRuleSet,
    dtrCompletionContextUpdater = dtrCompletionContextUpdater,
    caseRepository = caseRepository,
    cas3UpcomingRuleSet = cas3UpcomingRuleSet,
    cas3UpcomingContextUpdater = cas3UpcomingContextUpdater,
    cas1UpcomingRuleSet = cas1UpcomingRuleSet,
    cas1UpcomingContextUpdater = cas1UpcomingContextUpdater,
    cas1SuitabilityContextUpdater = cas1SuitabilityContextUpdater,
    crsEligibilityRuleSet = crsEligibilityRuleSet,
    crsCompletionRuleSet = crsCompletionRuleSet,
    crsContextUpdater = crsContextUpdater,
    cas3SuitabilityContextUpdater = cas3SuitabilityContextUpdater,
    paEligibilityRuleSet = paEligibilityRuleSet,
    paCompletionRuleSet = paCompletionRuleSet,
    paCompletionContextUpdater = paCompletionContextUpdater,
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
      val cpr = buildCorePersonRecord(
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
          tier = tier,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
        ),
      )
      val accommodationSummaryDto = buildAccommodationSummaryDto(endDate = endDate)
      val caseEntity = buildCaseEntity(id = caseId)
      val currentAccommodation = buildAccommodationSummaryDto(endDate = endDate, type = buildAccommodationTypeDto(code = "A02"))

      every { accommodationQueryService.getNextAccommodation(crn, cpr.addresses) } returns null
      every { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) } returns dutyToRefer
      every { accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses) } returns accommodationSummaryDto
      every { eligibilityOrchestrationService.getData(crn) } returns orchestrationDto
      every { caseRepository.findByCrn(crn) } returns caseEntity

      val result = eligibilityService.getDomainData(crn)
      assertThat(result.tierScore).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(SexCode.M)
      assertThat(result.cas1Application).isEqualTo(cas1Application)
      assertThat(result.cas3Application).isEqualTo(cas3Application)
      assertThat(result.currentAccommodation).isEqualTo(currentAccommodation)
      assertThat(result.nextAccommodation).isNull()
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
            expectedCas1Action = row["expectedCas1Action"],
            expectedCas1Link = row["expectedCas1Link"],
            expectedFailureReasons = row["expectedFailureReasons"]
              ?.takeIf { it.isNotBlank() }
              ?.split(",")
              ?.map { FailureReason.valueOf(it.trim()) }
              ?: emptyList(),
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
          buildAccommodationSummaryDto(endDate = it)
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
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
          .isEqualTo(s.expectedCas1Status)

        assertThat(result.action).isEqualTo(s.expectedCas1Action)
        assertThat(result.link).isEqualTo(s.expectedCas1Link)
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, failure reasons mismatch")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
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
            expectedDtrAction = row["expectedDtrAction"],
            expectedDtrLink = row["expectedDtrLink"],
            expectedFailureReasons = row["expectedFailureReasons"]
              ?.takeIf { it.isNotBlank() }
              ?.split(",")
              ?.map { FailureReason.valueOf(it.trim()) }
              ?: emptyList(),
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
          buildAccommodationSummaryDto(
            endDate = it,
            type = buildAccommodationTypeDto(
              code = if (s.isPrivateCurrentAccommodation.toBoolean()) {
                "A01A"
              } else {
                "A03"
              },
            ),
          )
        } ?: buildAccommodationSummaryDto(
          endDate = null,
          type = buildAccommodationTypeDto(
            code = if (s.isPrivateCurrentAccommodation.toBoolean()) {
              "A01A"
            } else {
              "A03"
            },
          ),
        )

        val nextAccommodation = if (s.hasNextAccommodation.toBoolean()) {
          buildAccommodationSummaryDto()
        } else {
          null
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          dutyToRefer = dutyToRefer,
          nextAccommodation = nextAccommodation,
          currentAccommodation = currentAccommodation,
        )

        val result = eligibilityService.calculateEligibilityForDtr(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedDtrStatus}")
          .isEqualTo(s.expectedDtrStatus)

        assertThat(result.action).isEqualTo(s.expectedDtrAction)
        assertThat(result.link).isEqualTo(s.expectedDtrLink)
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, failure reasons mismatch")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
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
            expectedCas3Action = row["expectedCas3Action"],
            expectedCas3Link = row["expectedCas3Link"],
            crsSubmissionDate = row["crsSubmissionDate"]?.toLocalDate(),
            isCrsStatusTerminated = row["isCrsStatusTerminated"]!!,
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

        val currentAccommodation = s.currentAccommodationEndDate?.let {
          buildAccommodationSummaryDto(
            endDate = it,
            type = buildAccommodationTypeDto(
              code = if (s.isPrisonCas1Cas2OrCas2v2CurrentAccommodation.toBoolean()) {
                "A02"
              } else {
                "A03"
              },
            ),
          )
        } ?: buildAccommodationSummaryDto(
          endDate = null,
          type = buildAccommodationTypeDto(
            code = if (s.isPrisonCas1Cas2OrCas2v2CurrentAccommodation.toBoolean()) {
              "A02"
            } else {
              "A03"
            },
          ),
        )

        val isCrsStatusTerminated = s.isCrsStatusTerminated.toBoolean()

        val commissionedRehabilitativeServices = s.crsSubmissionDate?.let {
          buildCommissionedRehabilitativeServices(
            submissionDate = s.crsSubmissionDate,
            status = if (isCrsStatusTerminated) CrsStatus.NSI_TERMINATED else CrsStatus.COMPLETED,
          )
        }

        val nextAccommodation = if (s.hasNextAccommodation.toBoolean()) {
          buildAccommodationSummaryDto()
        } else {
          null
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          currentAccommodation = currentAccommodation,
          nextAccommodation = nextAccommodation,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
          dutyToRefer = dutyToRefer,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        )

        val result = eligibilityService.calculateEligibilityForCas3(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedCas3Status}")
          .isEqualTo(s.expectedCas3Status)

        assertThat(result.action).isEqualTo(s.expectedCas3Action)
        assertThat(result.link).isEqualTo(s.expectedCas3Link)
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, failure reasons mismatch")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }
  }

  @Nested
  inner class CrsEligibilityScenarios {

    fun loadCrsScenarios(): List<CrsScenario> {
      val rows = CsvReader().read("/crs-eligibility-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          CrsScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            crsStatus = row["crsStatus"]?.let { CrsStatus.valueOf(it) },
            expectedCrsStatus = row["expectedCrsStatus"]?.let { ServiceStatus.valueOf(it) },
            expectedCrsAction = row["expectedCrsAction"],
            expectedCrsLink = row["expectedCrsLink"],
            sex = row["sex"]?.let { SexCode.valueOf(it) },
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
            submissionDate = s.crsSubmissionDate!!,
            status = it,
          )
        }

        val nextAccommodation = if (s.hasNextAccommodation.toBoolean()) {
          buildAccommodationSummaryDto()
        } else {
          null
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          sex = s.sex,
          currentAccommodation = currentAccommodation,
          nextAccommodation = nextAccommodation,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        )

        val result = eligibilityService.calculateEligibilityForCrs(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedCrsStatus}")
          .isEqualTo(s.expectedCrsStatus)

        assertThat(result.action).isEqualTo(s.expectedCrsAction)
        assertThat(result.link).isEqualTo(s.expectedCrsLink)
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, failure reasons mismatch")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }
  }

  @Nested
  inner class PaEligibilityScenarios {

    fun loadPaScenarios(): List<PaScenario> {
      val rows = CsvReader().read("/pa-eligibility-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          PaScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            isSubmittedCas1 = row["isSubmittedCas1"]!!,
            isSubmittedCas3 = row["isSubmittedCas3"]!!,
            expectedPaStatus = row["expectedPaStatus"]?.let { ServiceStatus.valueOf(it) },
            expectedPaAction = row["expectedPaAction"],
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
    fun `should calculate eligibility for pa for all scenarios`() {
      val scenarios = loadPaScenarios()

      runScenarios(scenarios) { s ->

        val cas1Application = if (s.isSubmittedCas1.toBoolean()) {
          buildCas1Application(
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
            placementStatus = Cas1PlacementStatus.UPCOMING,
          )
        } else {
          null
        }

        val cas3Application = if (s.isSubmittedCas3.toBoolean()) {
          buildCas3Application(
            applicationStatus = Cas3ApplicationStatus.SUBMITTED,
            assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
            bookingStatus = Cas3BookingStatus.CONFIRMED,
          )
        } else {
          null
        }

        val nextAccommodation = if (s.hasNextAccommodation.toBoolean()) {
          buildAccommodationSummaryDto()
        } else {
          null
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          nextAccommodation = nextAccommodation,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
        )

        val result = eligibilityService.calculateEligibilityForPa(data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedPaStatus}")
          .isEqualTo(s.expectedPaStatus)

        assertThat(result.action).isEqualTo(s.expectedPaAction)
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, failure reasons mismatch")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }
  }

  @Nested
  inner class FailureReasonsSmoke {

    private val today = LocalDate.parse("01/01/2025", dateFormatter)

    @Test
    fun `Cas1 surfaces S_TIER when candidate is on an S tier`() {
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.M,
        tierScore = TierScore.A1S,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
      )

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.S_TIER)
    }

    @Test
    fun `Cas1 surfaces MALE_NOT_HIGH_RISK_TIER for male candidate on a low-risk tier`() {
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.M,
        tierScore = TierScore.C3,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
      )

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.MALE_NOT_HIGH_RISK_TIER)
    }

    @Test
    fun `Cas1 surfaces NON_MALE_NOT_HIGH_RISK_TIER for non-male candidate on a low-risk tier`() {
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.F,
        tierScore = TierScore.D3,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
      )

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.NON_MALE_NOT_HIGH_RISK_TIER)
    }

    @Test
    fun `Cas1 surfaces SEX_DATA_NOT_AVAILABLE when candidate has no sex`() {
      clock.setNow(today)
      val data = buildDomainData(sex = null)

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.SEX_DATA_NOT_AVAILABLE)
    }

    @Test
    fun `Cas1 surfaces NO_CURRENT_ACCOMMODATION_END_DATE when end date is missing`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = null),
      )

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.NO_CURRENT_ACCOMMODATION_END_DATE)
    }

    @Test
    fun `Cas1 surfaces INVALID_APPLICATION_STATE when application has placement but unsuitable status`() {
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.M,
        tierScore = TierScore.A1,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.STARTED,
          placementStatus = Cas1PlacementStatus.ARRIVED,
          requestForPlacementStatus = null,
        ),
      )

      val result = eligibilityService.calculateEligibilityForCas1(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.INVALID_APPLICATION_STATE)
    }

    @Test
    fun `Cas3 surfaces INVALID_CURRENT_ACCOMMODATION_TYPE when accommodation is not prison or CAS1 or CAS2`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(
          endDate = today.plusDays(1),
          type = buildAccommodationTypeDto(code = "A03"),
        ),
        cas1Application = null,
        cas3Application = null,
        dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = today)),
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE)
    }

    @Test
    fun `Cas3 surfaces CONFLICTING_CAS1_BOOKING when there is an upcoming Cas1 placement`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
          placementStatus = Cas1PlacementStatus.UPCOMING,
          requestForPlacementStatus = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        ),
        cas3Application = null,
        dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = today)),
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.CONFLICTING_CAS1_BOOKING)
    }

    @Test
    fun `Cas3 surfaces CRS_EXPIRED when CRS submission is older than 12 weeks`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
        cas3Application = null,
        dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = today)),
        commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
          submissionDate = today.minusWeeks(13),
          status = CrsStatus.COMPLETED,
        ),
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.CRS_EXPIRED)
    }

    @Test
    fun `Cas3 surfaces CRS_NOT_SUBMITTED when CRS is in a non-submitted status`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
        cas3Application = null,
        dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = today)),
        commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
          submissionDate = today,
          status = CrsStatus.NSI_REFERRAL,
        ),
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.CRS_NOT_SUBMITTED)
    }

    @Test
    fun `Cas3 surfaces DTR_REFERRAL_EXPIRED when no DTR is present`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        cas1Application = null,
        cas3Application = null,
        dutyToRefer = null,
      )

      val result = eligibilityService.calculateEligibilityForCas3(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.DTR_REFERRAL_EXPIRED)
    }

    @Test
    fun `Dtr surfaces HAS_NEXT_ACCOMMODATION when candidate has next accommodation`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(
          endDate = today.plusDays(1),
          type = buildAccommodationTypeDto(code = "A03"),
        ),
        nextAccommodation = buildAccommodationSummaryDto(),
      )

      val result = eligibilityService.calculateEligibilityForDtr(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.HAS_NEXT_ACCOMMODATION)
    }

    @Test
    fun `Dtr surfaces CURRENT_ADDRESS_IS_PRIVATE when current address is private`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(
          endDate = today.plusDays(1),
          type = buildAccommodationTypeDto(code = "A01A"),
        ),
        nextAccommodation = null,
      )

      val result = eligibilityService.calculateEligibilityForDtr(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.CURRENT_ADDRESS_IS_PRIVATE)
    }

    @Test
    fun `Crs surfaces NOT_MALE when candidate is not male`() {
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.F,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusDays(1)),
        commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
          submissionDate = today.minusWeeks(13),
          status = CrsStatus.COMPLETED,
        ),
      )

      val result = eligibilityService.calculateEligibilityForCrs(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.NOT_MALE)
    }

    @Test
    fun `Pa surfaces SUITABLE_CAS1_APPLICATION when candidate has a suitable CAS1 application`() {
      val data = buildDomainData(
        nextAccommodation = null,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
          requestForPlacementStatus = null,
          placementStatus = null,
        ),
        cas3Application = null,
      )

      val result = eligibilityService.calculateEligibilityForPa(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.SUITABLE_CAS1_APPLICATION)
    }

    @Test
    fun `Pa surfaces SUITABLE_CAS3_APPLICATION when candidate has a suitable CAS3 application`() {
      val data = buildDomainData(
        nextAccommodation = null,
        cas1Application = null,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          assessmentStatus = null,
          bookingStatus = null,
        ),
      )

      val result = eligibilityService.calculateEligibilityForPa(data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.SUITABLE_CAS3_APPLICATION)
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
  val expectedCas1Action: String?,
  val expectedCas1Link: String?,
  val expectedFailureReasons: List<FailureReason>,
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
  val expectedDtrAction: String?,
  val expectedDtrLink: String?,
  val expectedFailureReasons: List<FailureReason>,
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
  val crsSubmissionDate: LocalDate?,
  val isCrsStatusTerminated: String,
  val cas3ApplicationStatus: Cas3ApplicationStatus?,
  val cas3AssessmentStatus: Cas3AssessmentStatus?,
  val cas3BookingStatus: Cas3BookingStatus?,
  val expectedCas3Status: ServiceStatus?,
  val expectedCas3Action: String?,
  val expectedCas3Link: String?,
  val expectedFailureReasons: List<FailureReason>,
)

data class CrsScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val hasNextAccommodation: String,
  val currentAccommodationEndDate: LocalDate?,
  val sex: SexCode?,
  val crsSubmissionDate: LocalDate?,
  val crsStatus: CrsStatus?,
  val expectedCrsStatus: ServiceStatus?,
  val expectedCrsAction: String?,
  val expectedCrsLink: String?,
  val expectedFailureReasons: List<FailureReason>,
)

data class PaScenario(
  val testCaseId: String,
  val description: String?,
  val hasNextAccommodation: String,
  val isSubmittedCas1: String,
  val isSubmittedCas3: String,
  val expectedPaStatus: ServiceStatus?,
  val expectedPaAction: String?,
  val expectedFailureReasons: List<FailureReason>,
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
