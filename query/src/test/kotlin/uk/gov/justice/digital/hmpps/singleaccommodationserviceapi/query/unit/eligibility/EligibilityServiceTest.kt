package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer.toUpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityTreeProvider
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinOneYearRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationPresentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3BookingSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.ReleaseWithinFourWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrApplicationCompleteRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrNotWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.ReleaseWithinEightWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas1ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas3ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.PaEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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
  private val accommodationTypeRepository = mockk<AccommodationTypeRepository>()

  // CAS1
  var cas1UiUrl = "CAS1_UI_URL"
  var cas1CompletionContextUpdater = Cas1CompletionContextUpdater(cas1UiUrl)
  var cas1ValidationRuleSet = Cas1ValidationRuleSet(
    Cas1SexValidationRule(),
  )
  var cas1CompletionRuleSet = Cas1CompletionRuleSet(Cas1ApplicationCompletionRule())
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(Cas1ApplicationSuitabilityRule())
  var cas1EligibilityRuleSet = Cas1EligibilityRuleSet(
    STierEligibilityRule(),
    MaleRiskEligibilityRule(),
    NonMaleRiskEligibilityRule(),
  )
  val cas1UpcomingContextUpdater = Cas1UpcomingContextUpdater(clock)
  var cas1UpcomingRuleSet = Cas1UpcomingRuleSet(ReleaseWithinOneYearRule(clock))
  val cas1SuitabilityContextUpdater = Cas1SuitabilityContextUpdater(cas1UiUrl)

  // CAS3
  var cas3UiUrl = "CAS3_UI_URL"
  var cas3SuitabilityContextUpdater = Cas3SuitabilityContextUpdater(cas3UiUrl)
  var cas3CompletionContextUpdater = Cas3CompletionContextUpdater(cas3UiUrl)
  val cas3UpcomingContextUpdater = Cas3UpcomingContextUpdater(clock)
  var cas3UpcomingRuleSet = Cas3UpcomingRuleSet(ReleaseWithinFourWeeksRule(clock))
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(
    Cas3ApplicationSuitabilityRule(),
    Cas3ApplicationPresentSuitabilityRule(),
    Cas3BookingSuitabilityRule(),
    Cas3AssessmentSuitabilityRule(),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(Cas3ApplicationCompletionRule())
  var cas3EligibilityRuleSet = Cas3EligibilityRuleSet(
    CurrentAccommodationTypeRule(),
    NoNextAccommodationRule(),
    DtrExpiredReferralRule(clock),
    CrsSubmittedRule(),
    CrsExpiredRule(clock),
  )

  // DTR
  var dtrUpcomingRuleSet = DtrUpcomingRuleSet(ReleaseWithinEightWeeksRule(clock))
  var dtrUpcomingContextUpdater = DtrUpcomingContextUpdater(clock)
  var dtrSuitabilityRuleSet = DtrSuitabilityRuleSet(
    DtrPresentRule(),
    DtrNotWithdrawnRule(),
    DtrExpiredReferralRule(clock),
  )
  var dtrCompletionContextUpdater = DtrCompletionContextUpdater()
  var dtrCompletionRuleSet = DtrCompletionRuleSet(DtrApplicationCompleteRule())
  var dtrEligibilityRuleSet = DtrEligibilityRuleSet(
    NoNextAccommodationRule(),
  )

  // CRS
  var crsUiUrl = "CRS_UI_URL"
  var crsEligibilityRuleSet = CrsEligibilityRuleSet(
    NoNextAccommodationRule(),
  )
  var crsCompletionRuleSet = CrsCompletionRuleSet(
    CrsSubmittedRule(),
    CrsExpiredRule(clock),
  )
  var crsCompletionContextUpdater = CrsCompletionContextUpdater(crsUiUrl)
  var crsUpcomingRuleSet = CrsUpcomingRuleSet(CrsUpcomingRule(clock))
  var crsUpcomingContextUpdater = CrsUpcomingContextUpdater(clock)

  // PA
  var paEligibilityRuleSet = PaEligibilityRuleSet(
    Cas1ApplicationNotSuitableRule(),
    Cas3ApplicationNotSuitableRule(),
  )
  var paCompletionRuleSet = PaCompletionRuleSet(
    HasNextAccommodationRule(),
  )

  private val builder = DecisionTreeBuilder(rulesEngine)

  private val cas1Tree = Cas1EligibilityTreeProvider(
    builder = builder,
    validation = cas1ValidationRuleSet,
    upcoming = cas1UpcomingRuleSet,
    upcomingContextUpdater = cas1UpcomingContextUpdater,
    suitability = cas1SuitabilityRuleSet,
    suitabilityContextUpdater = cas1SuitabilityContextUpdater,
    completion = cas1CompletionRuleSet,
    completionContextUpdater = cas1CompletionContextUpdater,
    eligibility = cas1EligibilityRuleSet,
    approvedPremisesUiBaseUrl = cas1UiUrl,
  )

  private val cas3Tree = Cas3EligibilityTreeProvider(
    builder = builder,
    upcoming = cas3UpcomingRuleSet,
    upcomingContextUpdater = cas3UpcomingContextUpdater,
    suitability = cas3SuitabilityRuleSet,
    suitabilityContextUpdater = cas3SuitabilityContextUpdater,
    completion = cas3CompletionRuleSet,
    completionContextUpdater = cas3CompletionContextUpdater,
    eligibility = cas3EligibilityRuleSet,
    temporaryAccommodationUiBaseUrl = cas3UiUrl,
  )

  private val dtrTree = DtrEligibilityTreeProvider(
    builder = builder,
    upcoming = dtrUpcomingRuleSet,
    upcomingContextUpdater = dtrUpcomingContextUpdater,
    suitability = dtrSuitabilityRuleSet,
    completion = dtrCompletionRuleSet,
    completionContextUpdater = dtrCompletionContextUpdater,
    eligibility = dtrEligibilityRuleSet,
  )

  private val crsTree = CrsEligibilityTreeProvider(
    builder = builder,
    eligibility = crsEligibilityRuleSet,
    completion = crsCompletionRuleSet,
    completionContextUpdater = crsCompletionContextUpdater,
    upcoming = crsUpcomingRuleSet,
    upcomingContextUpdater = crsUpcomingContextUpdater,
    crsUiBaseUrl = crsUiUrl,
  )

  private val paTree = PaEligibilityTreeProvider(
    builder = builder,
    eligibility = paEligibilityRuleSet,
    completion = paCompletionRuleSet,
  )

  private val eligibilityService = EligibilityService(
    accommodationQueryService = accommodationQueryService,
    accommodationTypeRepository = accommodationTypeRepository,
    caseRepository = caseRepository,
    dutyToReferQueryService = dutyToReferQueryService,
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    cas1Tree = cas1Tree,
    cas3Tree = cas3Tree,
    dtrTree = dtrTree,
    crsTree = crsTree,
    paTree = paTree,
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
          buildCanonicalAddress(
            status = CanonicalAddressStatus(
              code = AddressStatusCode.M.name,
              description = AddressStatusCode.M.description,
            ),
            endDate = endDate,
            usage = CanonicalAddressUsage(
              usageCode = CanonicalAddressUsageCode(
                code = AddressUsageCode.A02.name,
                description = AddressUsageCode.A02.description,
              ),
              isActive = true,
            ),
          ),
        ),
      )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val dutyToRefer = buildDutyToReferDto(crn, UUID.randomUUID(), DtrStatus.SUBMITTED, submission = null)
      val crs = buildCommissionedRehabilitativeServices()
      val prisoner = buildPrisoner()
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = cpr,
          tier = tier,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
          commissionedRehabilitativeServices = listOf(crs),
          prisoner = prisoner,
        ),
      )
      val caseEntity = buildCaseEntity(id = caseId)
      val currentAccommodation = buildAccommodationSummaryDto(endDate = endDate, type = buildAccommodationTypeDto(code = "A02"))
      val accommodationTypeEntity = buildAccommodationTypeEntity(isPrison = true, code = "A02")

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationTypeEntity)
      every { accommodationQueryService.getNextAccommodations(crn, cpr.addresses, cas1Application, cas3Application, currentAccommodation) } returns emptyList()
      every { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) } returns dutyToRefer
      every { accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses, prisoner) } returns currentAccommodation
      val result = eligibilityService.buildDomainData(crn, orchestrationDto.data, caseEntity)

      val expected = buildDomainData(
        crn = crn,
        tierScore = expectedTier,
        sex = cpr.sex!!.code,
        currentAccommodation = currentAccommodation,
        currentAccommodationTypeEntity = accommodationTypeEntity,
        nextAccommodations = emptyList(),
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        commissionedRehabilitativeServices = crs,
        dutyToRefer = dutyToRefer,
      )

      assertThat(result).isEqualTo(expected)
    }
  }

  @Nested
  inner class GetEligibility {
    val crn = "ABC123"

    @Test
    fun `getEligibility returns failed eligibility and upstream failures when external api fails`() {
      val upstreamFailure = buildUpstreamFailure()
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = null,
          tier = null,
          cas1Application = null,
          cas3Application = null,
          commissionedRehabilitativeServices = null,
          prisoner = null,
        ),
        upstreamFailures = listOf(
          upstreamFailure,
        ),
      )

      every { caseRepository.findByCrn(crn) } returns null
      every { eligibilityOrchestrationService.getData(crn, null) } returns orchestrationDto

      val result = eligibilityService.getEligibility(crn)

      val expected = ApiResponseDto(
        data = buildEligibilityDto(crn),
        upstreamFailures = listOf(toUpstreamFailureDto(upstreamFailure)),
      )
      assertThat(result).isEqualTo(expected)
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

        val result = eligibilityService.evaluate(cas1Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
          .isEqualTo(s.expectedCas1Status)

        assertThat(result.action).isEqualTo(s.expectedCas1Action)
        assertThat(result.link).isEqualTo(s.expectedCas1Link)
        if (s.expectedCas1Link == null) {
          assertThat(result.url).isNull()
        } else {
          assertThat(result.url).isEqualTo(cas1UiUrl)
        }
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
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

        val currentAccommodation = buildAccommodationSummaryDto(
          endDate = s.currentAccommodationEndDate,
        )

        val currentAccommodationTypeEntity = currentAccommodation.type?.code?.let {
          buildAccommodationTypeEntity(
            code = it,
          )
        }

        val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
          listOf(buildAccommodationSummaryDto())
        } else {
          emptyList()
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          dutyToRefer = dutyToRefer,
          nextAccommodations = nextAccommodations,
          currentAccommodation = currentAccommodation,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        )

        val result = eligibilityService.evaluate(dtrTree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedDtrStatus}")
          .isEqualTo(s.expectedDtrStatus)

        assertThat(result.action).isEqualTo(s.expectedDtrAction)
        assertThat(result.link).isEqualTo(s.expectedDtrLink)
        assertThat(result.url).isNull()
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
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

        val currentAccommodationTypeEntity = currentAccommodation.type?.code?.let {
          buildAccommodationTypeEntity(
            code = it,
            isCas1 = s.isPrisonCas1Cas2OrCas2v2CurrentAccommodation.toBoolean(),
          )
        }

        val isCrsStatusTerminated = s.isCrsStatusTerminated.toBoolean()

        val commissionedRehabilitativeServices = s.crsSubmissionDate?.let {
          buildCommissionedRehabilitativeServices(
            sentAt = s.crsSubmissionDate.atStartOfDay().atOffset(ZoneOffset.UTC),
            status = if (isCrsStatusTerminated) CrsReferralStatus.WITHDRAWN else CrsReferralStatus.COMPLETED,
          )
        }

        val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
          listOf(buildAccommodationSummaryDto())
        } else {
          emptyList()
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = null,
          currentAccommodation = currentAccommodation,
          nextAccommodations = nextAccommodations,
          cas3Application = cas3Application,
          dutyToRefer = dutyToRefer,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        )

        val result = eligibilityService.evaluate(cas3Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedCas3Status}")
          .isEqualTo(s.expectedCas3Status)

        assertThat(result.action).isEqualTo(s.expectedCas3Action)
        assertThat(result.link).isEqualTo(s.expectedCas3Link)
        if (s.expectedCas3Link == null) {
          assertThat(result.url).isNull()
        } else {
          assertThat(result.url).isEqualTo(cas3UiUrl)
        }
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
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
            sex = row["sex"]?.takeIf { it.isNotBlank() }?.let { SexCode.valueOf(it) },
            hasNextAccommodation = row["hasNextAccommodation"]!!,
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            crsStatus = row["crsStatus"]?.let { CrsReferralStatus.valueOf(it) },
            expectedCrsStatus = row["expectedCrsStatus"]?.let { ServiceStatus.valueOf(it) },
            expectedCrsAction = row["expectedCrsAction"],
            expectedCrsLink = row["expectedCrsLink"],
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

        val data = buildDomainData(
          crn = s.testCaseId,
          sex = s.sex,
          currentAccommodation = currentAccommodation,
          nextAccommodations = nextAccommodations,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
        )

        val result = eligibilityService.evaluate(crsTree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedCrsStatus}")
          .isEqualTo(s.expectedCrsStatus)

        assertThat(result.action).isEqualTo(s.expectedCrsAction)
        assertThat(result.link).isEqualTo(s.expectedCrsLink)
        if (s.expectedCrsLink == null) {
          assertThat(result.url).isNull()
        } else {
          assertThat(result.url).isEqualTo(crsUiUrl)
        }
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
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

        val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
          listOf(buildAccommodationSummaryDto())
        } else {
          emptyList()
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          nextAccommodations = nextAccommodations,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
        )

        val result = eligibilityService.evaluate(paTree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, actual: ${result.serviceStatus}, expected: ${s.expectedPaStatus}")
          .isEqualTo(s.expectedPaStatus)

        assertThat(result.action).isEqualTo(s.expectedPaAction)
        assertThat(result.link).isNull()
        assertThat(result.url).isNull()

        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
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

      val result = eligibilityService.evaluate(cas1Tree, data)

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

      val result = eligibilityService.evaluate(cas1Tree, data)

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

      val result = eligibilityService.evaluate(cas1Tree, data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.NON_MALE_NOT_HIGH_RISK_TIER)
    }

    @Test
    fun `Cas1 surfaces SEX_DATA_NOT_AVAILABLE when candidate has no sex`() {
      clock.setNow(today)
      val data = buildDomainData(sex = null)

      val result = eligibilityService.evaluate(cas1Tree, data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.SEX_DATA_NOT_AVAILABLE)
    }

    @Test
    fun `Cas3 surfaces INVALID_CURRENT_ACCOMMODATION_TYPE when accommodation is not prison or CAS1 or CAS2`() {
      clock.setNow(today)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(
          endDate = today.plusDays(1),
        ),
        currentAccommodationTypeEntity = buildAccommodationTypeEntity(isPrison = false, isCas1 = false, isCas2 = false),
        cas1Application = null,
        cas3Application = null,
        dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = today)),
      )

      val result = eligibilityService.evaluate(cas3Tree, data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE)
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
          sentAt = today.minusWeeks(13).atStartOfDay().atOffset(ZoneOffset.UTC),
          status = CrsReferralStatus.COMPLETED,
        ),
      )

      val result = eligibilityService.evaluate(cas3Tree, data)

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
          sentAt = today.atStartOfDay().atOffset(ZoneOffset.UTC),
          status = CrsReferralStatus.DRAFT,
        ),
      )

      val result = eligibilityService.evaluate(cas3Tree, data)

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

      val result = eligibilityService.evaluate(cas3Tree, data)

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
        nextAccommodations = listOf(buildAccommodationSummaryDto()),
      )

      val result = eligibilityService.evaluate(dtrTree, data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.HAS_NEXT_ACCOMMODATION)
    }

    @Test
    fun `Pa surfaces SUITABLE_CAS1_APPLICATION when candidate has a suitable CAS1 application`() {
      val data = buildDomainData(
        nextAccommodations = emptyList(),
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
          requestForPlacementStatus = null,
          placementStatus = null,
        ),
        cas3Application = null,
      )

      val result = eligibilityService.evaluate(paTree, data)

      assertThat(result.serviceStatus).isEqualTo(ServiceStatus.NOT_ELIGIBLE)
      assertThat(result.failureReasons).contains(FailureReason.SUITABLE_CAS1_APPLICATION)
    }

    @Test
    fun `Pa surfaces SUITABLE_CAS3_APPLICATION when candidate has a suitable CAS3 application`() {
      val data = buildDomainData(
        nextAccommodations = emptyList(),
        cas1Application = null,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          assessmentStatus = null,
          bookingStatus = null,
        ),
      )

      val result = eligibilityService.evaluate(paTree, data)

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
  val sex: SexCode?,
  val hasNextAccommodation: String,
  val currentAccommodationEndDate: LocalDate?,
  val crsSubmissionDate: LocalDate?,
  val crsStatus: CrsReferralStatus?,
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
