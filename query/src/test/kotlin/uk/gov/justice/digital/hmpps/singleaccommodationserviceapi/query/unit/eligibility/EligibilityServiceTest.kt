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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer.toUpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.CAS1_UPCOMING_WEEKS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityNewToOldTransformer.toEligibilityDtoOld
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityTierRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationRelevantExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinTwentyEightWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationAwaitingArrivalRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityPersonRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityReleaseDateRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.InPrisonNoReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.SexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.SingleLetterTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2ApplicationSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitableStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3ApplicationArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationPresentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3BookingSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilitySettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.IsSettledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrEligibilityTreeProvider
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.CsvReader
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(value = [MockKExtension::class])
class EligibilityServiceTest {

  private val clock = MutableClock()

  var rulesEngine = RulesEngine(DefaultRuleSetEvaluator())

  private val accommodationSummaryCalculator = mockk<AccommodationSummaryCalculator>()
  private val sentryService = mockk<SentryService>()

  private val eligibilityOrchestrationService = mockk<EligibilityOrchestrationService>()
  private val dutyToReferQueryService = mockk<DutyToReferQueryService>()

  private val caseRepository = mockk<CaseRepository>()
  private val accommodationTypeRepository = mockk<AccommodationTypeRepository>()

  // CAS1
  var cas1ApplicationStartUrl = "CAS1_APPLICATION_START_URL"
  var cas1CompletionContextUpdater = Cas1CompletionContextUpdater()
  var cas1SuitabilityRuleSet = Cas1SuitabilityRuleSet(Cas1ApplicationPresentRule(), Cas1ApplicationSuitabilityRule(), Cas1ApplicationRelevantExpiredRule())
  var cas1EligibilityTierRuleSet = Cas1EligibilityTierRuleSet(
    MaleRiskEligibilityRule(),
    NonMaleRiskEligibilityRule(),
  )
  var cas1EligibilityCurrentAccommodationRuleSet = Cas1EligibilityCurrentAccommodationRuleSet(
    CurrentAccommodationIsPrisonOrCas1Rule(),
  )
  val cas1UpcomingContextUpdater = Cas1UpcomingContextUpdater()
  var cas1UpcomingRuleSet = Cas1UpcomingRuleSet(ReleaseWithinTwentyEightWeeksRule(clock))
  val cas1SuitabilityContextUpdater = Cas1SuitabilityContextUpdater()

  // CAS2
  var cas2ApplicationStartUrl = "CAS2_APPLICATION_START_URL#crn"
  var cas2CompletionContextUpdater = Cas2CompletionContextUpdater(sentryService)

  var cas2CompletionRuleSet = Cas2CompletionRuleSet(
    Cas2ApplicationAwaitingArrivalRule(),
    Cas2ApplicationArrivedRule(),
  )
  var cas2SuitabilityRuleSet = Cas2SuitabilityRuleSet(Cas2ApplicationSubmittedRule(), Cas2SuitableStatusRule())
  var cas2EligibilityPersonRuleSet = Cas2EligibilityPersonRuleSet(
    Under18Rule(clock),
    SingleLetterTierRule(),
    SexualOffenceRegistrationsRule(),
  )
  var cas2EligibilityReleaseDateRuleSet = Cas2EligibilityReleaseDateRuleSet(
    InPrisonNoReleaseDateRule(),
  )
  var cas2EligibilityCurrentAccommodationRuleSet = Cas2EligibilityCurrentAccommodationRuleSet(
    CurrentAccommodationTypeIsCas3Rule(),
    CurrentAccommodationTypeIsCas2Rule(),
    CurrentAccommodationTypeIsCommunityRule(),
  )
  var cas2EligibilityNextAccommodationRuleSet = Cas2EligibilityNextAccommodationRuleSet(
    NoNextAccommodationRule(),
  )
  val cas2UpcomingContextUpdater = Cas2UpcomingContextUpdater()
  var cas2UpcomingRuleSet = Cas2UpcomingRuleSet(ReleaseWithin15DaysRule(clock))
  val cas2SuitabilityContextUpdater = Cas2SuitabilityContextUpdater(sentryService)

  // CAS3
  var cas3ReferralStartUrl = "CAS3_REFERRAL_START_URL"
  var cas3UpcomingRuleSet = Cas3UpcomingRuleSet(ReleaseWithin15DaysRule(clock))
  var cas3UpcomingContextUpdater = Cas3UpcomingContextUpdater()
  var cas3SuitabilityContextUpdater = Cas3SuitabilityContextUpdater()
  var cas3CompletionContextUpdater = Cas3CompletionContextUpdater()
  var cas3SuitabilityRuleSet = Cas3SuitabilityRuleSet(
    Cas3ApplicationSuitabilityRule(),
    Cas3ApplicationPresentSuitabilityRule(),
    Cas3BookingSuitabilityRule(),
    Cas3AssessmentSuitabilityRule(),
  )
  var cas3CompletionRuleSet = Cas3CompletionRuleSet(Cas3ApplicationArrivedRule())
  var cas3EligibilityCurrentAccommodationRuleSet = Cas3EligibilityCurrentAccommodationRuleSet(
    CurrentAccommodationTypeRule(),
  )
  var cas3EligibilityNextAccommodationRuleSet = Cas3EligibilityNextAccommodationRuleSet(
    NoNextAccommodationRule(),
  )
  var cas3PrerequisiteRuleSet = Cas3PrerequisiteRuleSet(
    DtrExpiredReferralRule(clock),
    CrsSubmittedRuleMale(),
    CrsSubmittedRuleNonMale(),
  )
  var cas3PrerequisiteContextUpdater = Cas3PrerequisiteContextUpdater()

  // DTR
  var dtrUpcomingRuleSet = DtrUpcomingRuleSet(ReleaseWithinEightWeeksRule(clock))
  var dtrUpcomingContextUpdater = DtrUpcomingContextUpdater()
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
  var crsEligibilitySettledRuleSet = CrsEligibilitySettledRuleSet(
    IsSettledRule(),
  )
  var crsEligibilityNextAccommodationRuleSet = CrsEligibilityNextAccommodationRuleSet(
    NoNextAccommodationRule(),
  )
  var crsCompletionRuleSet = CrsCompletionRuleSet(
    CrsSubmittedRule(),
  )
  var crsCompletionContextUpdater = CrsCompletionContextUpdater(crsUiUrl)
  var crsUpcomingRuleSet = CrsUpcomingRuleSet(CrsUpcomingRule(clock))
  var crsUpcomingContextUpdater = CrsUpcomingContextUpdater()

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
    upcoming = cas1UpcomingRuleSet,
    upcomingContextUpdater = cas1UpcomingContextUpdater,
    suitability = cas1SuitabilityRuleSet,
    suitabilityContextUpdater = cas1SuitabilityContextUpdater,
    completionContextUpdater = cas1CompletionContextUpdater,
    eligibilityTier = cas1EligibilityTierRuleSet,
    eligibilityCurrentAccommodation = cas1EligibilityCurrentAccommodationRuleSet,
  )

  private val cas2Tree = Cas2EligibilityTreeProvider(
    builder = builder,
    upcoming = cas2UpcomingRuleSet,
    upcomingContextUpdater = cas2UpcomingContextUpdater,
    suitability = cas2SuitabilityRuleSet,
    suitabilityContextUpdater = cas2SuitabilityContextUpdater,
    completion = cas2CompletionRuleSet,
    completionContextUpdater = cas2CompletionContextUpdater,
    eligibilityPerson = cas2EligibilityPersonRuleSet,
    eligibilityReleaseDate = cas2EligibilityReleaseDateRuleSet,
    eligibilityCurrentAccommodation = cas2EligibilityCurrentAccommodationRuleSet,
    eligibilityNextAccommodation = cas2EligibilityNextAccommodationRuleSet,
  )

  private val cas3Tree = Cas3EligibilityTreeProvider(
    builder = builder,
    upcoming = cas3UpcomingRuleSet,
    upcomingContextUpdater = cas3UpcomingContextUpdater,
    suitability = cas3SuitabilityRuleSet,
    suitabilityContextUpdater = cas3SuitabilityContextUpdater,
    completion = cas3CompletionRuleSet,
    completionContextUpdater = cas3CompletionContextUpdater,
    eligibilityCurrentAccommodation = cas3EligibilityCurrentAccommodationRuleSet,
    eligibilityNextAccommodation = cas3EligibilityNextAccommodationRuleSet,
    prerequisite = cas3PrerequisiteRuleSet,
    cas3PrerequisiteContextUpdater = cas3PrerequisiteContextUpdater,
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
    eligibilitySettled = crsEligibilitySettledRuleSet,
    eligibilityNextAccommodation = crsEligibilityNextAccommodationRuleSet,
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

  private val approvedPremisesCachingService = mockk<ApprovedPremisesCachingService> {
    every { getCas1UrlTemplates() } returns Cas1UrlTemplates(cas1ApplicationStartUrl)
    every { getCas2UrlTemplates() } returns Cas2UrlTemplates(cas2ApplicationStartUrl)
    every { getCas3UrlTemplates() } returns Cas3UrlTemplates(cas3ReferralStartUrl)
  }

  private val eligibilityService = EligibilityService(
    accommodationSummaryCalculator = accommodationSummaryCalculator,
    accommodationTypeRepository = accommodationTypeRepository,
    caseRepository = caseRepository,
    dutyToReferQueryService = dutyToReferQueryService,
    eligibilityOrchestrationService = eligibilityOrchestrationService,
    deeplinkResolver = DeeplinkResolver(approvedPremisesCachingService),
    cas1Tree = cas1Tree,
    cas2Tree = cas2Tree,
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
      val expectedTier = "A1"
      val caseId = UUID.randomUUID()
      val cas1Application = buildCas1Application()
      val cas2Application = buildCas2Application()
      val cas1CurrentPremises = buildCas1PremisesSummary()
      val cas3CurrentPremises = buildCas3PremisesSummary()
      val cas3Application = buildCas3Application()
      val cpr = buildCorePersonRecord(
        dateOfBirth = LocalDate.of(1990, 1, 1),
        addresses = listOf(
          buildCanonicalAddress(
            status = CanonicalAddressStatus(
              code = AddressStatusCode.M.name,
              description = AddressStatusCode.M.description,
            ),
            endDate = endDate,
            usages = listOf(
              CanonicalAddressUsage(
                usageCode = CanonicalAddressUsageCode(
                  code = AddressUsageCode.A02.name,
                  description = AddressUsageCode.A02.description,
                ),
                isActive = true,
              ),
            ),
          ),
        ),
      )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val dutyToRefer = buildDutyToReferDto(crn, UUID.randomUUID(), DtrStatus.SUBMITTED, submission = null)
      val crs = buildCommissionedRehabilitativeServices(status = CrsReferralStatus.LIVE)
      val prisoner = buildPrisoner()
      val registrations = buildSexualOffenceRegistrations(crn)
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = cpr,
          tier = tier,
          cas1Application = cas1Application,
          cas2Application = cas2Application,
          cas3Application = cas3Application,
          commissionedRehabilitativeServices = listOf(crs),
          prisoner = prisoner,
          cas1CurrentPremises = cas1CurrentPremises,
          cas3CurrentPremises = cas3CurrentPremises,
          sexualOffenceRegistrations = registrations,
        ),
      )
      val caseEntity = buildCaseEntity(id = caseId)
      val currentAccommodation = buildAccommodationSummaryDto(endDate = endDate, type = buildAccommodationTypeDto(code = "A02"))
      val accommodationTypeEntity = buildAccommodationTypeEntity(isPrison = true, code = "A02")

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationTypeEntity)
      every { accommodationSummaryCalculator.calculateNextAccommodations(crn, cpr.addresses, cas1Application, cas3Application, currentAccommodation) } returns emptyList()
      every { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) } returns dutyToRefer
      every { accommodationSummaryCalculator.calculateCurrentAccommodation(crn, cpr.addresses, prisoner, cas1CurrentPremises, cas3CurrentPremises) } returns currentAccommodation
      val result = eligibilityService.buildDomainData(crn, orchestrationDto.data, caseEntity)

      val expected = buildDomainData(
        crn = crn,
        tierScore = expectedTier,
        sex = cpr.sex!!.code,
        dob = cpr.dateOfBirth,
        currentAccommodation = currentAccommodation,
        currentAccommodationTypeEntity = accommodationTypeEntity,
        nextAccommodations = emptyList(),
        cas1Application = cas1Application,
        cas2Application = cas2Application,
        cas3Application = cas3Application,
        commissionedRehabilitativeServices = crs,
        dutyToRefer = dutyToRefer,
        sexualOffenceRegistrations = registrations,
      )

      assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `getDomainData selects the most recent live referral and ignores the rest`() {
      val cpr = buildCorePersonRecord()
      val currentAccommodation = buildAccommodationSummaryDto()
      val latestLiveReferral = buildCommissionedRehabilitativeServices(
        status = CrsReferralStatus.LIVE,
        sentAt = OffsetDateTime.now().minusDays(30),
      )
      val orchestrationDto = EligibilityOrchestrationDto(
        crn = crn,
        cpr = cpr,
        tier = null,
        cas1Application = null,
        cas2Application = null,
        cas3Application = null,
        commissionedRehabilitativeServices = listOf(
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.COMPLETED, sentAt = OffsetDateTime.now()),
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.WITHDRAWN, sentAt = OffsetDateTime.now()),
          latestLiveReferral,
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.LIVE, sentAt = OffsetDateTime.now().minusDays(60)),
        ),
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
        prisoner = null,
        sexualOffenceRegistrations = null,
      )

      every { accommodationTypeRepository.findAll() } returns emptyList()
      every { accommodationSummaryCalculator.calculateCurrentAccommodation(crn, cpr.addresses, null, null, null) } returns currentAccommodation
      every { accommodationSummaryCalculator.calculateNextAccommodations(crn, cpr.addresses, null, null, currentAccommodation) } returns emptyList()

      val result = eligibilityService.buildDomainData(crn, orchestrationDto, caseEntity = null)

      assertThat(result.commissionedRehabilitativeServices).isEqualTo(latestLiveReferral)
      assertThat(result.sexualOffenceRegistrations).isNull()
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
          cas2Application = null,
          cas3Application = null,
          commissionedRehabilitativeServices = null,
          prisoner = null,
          cas1CurrentPremises = null,
          cas3CurrentPremises = null,
          sexualOffenceRegistrations = null,
        ),
        upstreamFailures = listOf(
          upstreamFailure,
        ),
      )

      every { caseRepository.findByCrn(crn) } returns null
      every { eligibilityOrchestrationService.getData(crn, null) } returns orchestrationDto

      val result = eligibilityService.getEligibility(crn)

      val eligibility = buildEligibilityDtoNew(crn)

      val expected = ApiResponseDto(
        data = toEligibilityDtoOld(eligibility),
        upstreamFailures = listOf(toUpstreamFailureDto(upstreamFailure)),
      )
      assertThat(result).isEqualTo(expected)
    }
  }

  @Nested
  inner class Cas1EligibilityScenarios {

    fun loadCas1EligibilityScenarios(): List<Cas1EligibilityScenario> {
      val rows = CsvReader().read("/cas1-eligibility-scenarios.csv")

      return rows.flatMapIndexed { idx, row ->
        val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()

        try {
          accommodationTypes.map { accommodationType ->
            Cas1EligibilityScenario(
              testCaseId = row["testCaseId"]!!,
              description = row["description"],
              referenceDate = row["referenceDate"]!!.toLocalDate(),
              sex = row["sex"]?.let { SexCode.valueOf(it) },
              tierScore = row["tierScore"],
              currentAccommodationType = accommodationType,
              currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
              expectedCas1Status = row["expectedCas1Status"]?.let { ServiceStatusNew.valueOf(it) },
              expectedCas1Url = row["expectedCas1Url"],
              expectedFailureReasons = row["expectedFailureReasons"]
                ?.takeIf { it.isNotBlank() }
                ?.split(",")
                ?.map { FailureReason.valueOf(it.trim()) }
                ?: emptyList(),
            )
          }
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    fun loadCas1ApplicationScenarios(): List<Cas1ApplicationScenario> = CsvReader().read("/cas1-application-scenarios.csv").mapIndexed { idx, row ->
      try {
        Cas1ApplicationScenario(
          testCaseId = row["testCaseId"]!!,
          description = row["description"],
          referenceDate = row["referenceDate"]!!.toLocalDate(),
          currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
          isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
          cas1ApplicationStatus = row["cas1ApplicationStatus"]?.let { Cas1ApplicationStatus.valueOf(it) },
          cas1RequestForPlacementStatus = row["cas1RequestForPlacementStatus"]?.let {
            Cas1RequestForPlacementStatus.valueOf(it)
          },
          cas1PlacementStatus = row["cas1PlacementStatus"]?.let { Cas1PlacementStatus.valueOf(it) },
          expectedCas1Status = row["expectedCas1Status"]?.let { ServiceStatusNew.valueOf(it) },
          expectedCas1Url = row["expectedCas1Url"],
        )
      } catch (e: Exception) {
        throw IllegalStateException("Row $idx failed: $row", e)
      }
    }

    @Test
    fun `should calculate eligibility for cas1 for all scenarios`() {
      val scenarios = loadCas1EligibilityScenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val currentAccommodation = s.currentAccommodationType?.let {
          buildAccommodationSummaryDto(
            type = buildAccommodationTypeDto(code = it),
            endDate = s.currentAccommodationEndDate,
          )
        }

        val currentAccommodationTypeEntity = s.currentAccommodationType?.let {
          buildAccommodationTypeEntity(
            code = it,
            isPrison = it == "HMP",
            isCas1 = it == "A02",
          )
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = s.tierScore,
          sex = s.sex,
          currentAccommodation = currentAccommodation,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
          cas1Application = null,
        )

        val result = eligibilityService.evaluate(cas1Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
          .isEqualTo(s.expectedCas1Status)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusWeeks(CAS1_UPCOMING_WEEKS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS1)
        val expectedUrl = when (s.expectedCas1Url) {
          null -> null
          "/applications/start" -> cas1ApplicationStartUrl
          else -> error("Unexpected CAS1 eligibility URL: ${s.expectedCas1Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)

        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }

    @Test
    fun `should calculate application for cas1 for all scenarios`() {
      val scenarios = loadCas1ApplicationScenarios()

      runScenarios(scenarios) { s ->
        clock.setNow(s.referenceDate)

        val cas1Application = s.cas1ApplicationStatus?.let {
          buildCas1Application(
            application = buildCas1ApplicationSummary(status = it),
            placement = buildCas1PlacementSummary(status = s.cas1PlacementStatus),
            requestForPlacement = buildCas1RequestForPlacementSummary(status = s.cas1RequestForPlacementStatus),
          )
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = if (s.isEligible) "A" else "D",
          sex = SexCode.M,
          currentAccommodation = buildAccommodationSummaryDto(
            type = buildAccommodationTypeDto(code = "A02"),
            endDate = s.currentAccommodationEndDate,
          ),
          currentAccommodationTypeEntity = buildAccommodationTypeEntity(code = "A02", isCas1 = true),
          cas1Application = cas1Application,
        )

        val result = eligibilityService.evaluate(cas1Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas1Status}")
          .isEqualTo(s.expectedCas1Status)
        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusWeeks(CAS1_UPCOMING_WEEKS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS1)
        val expectedUrl = when (s.expectedCas1Url) {
          null -> null
          "/applications/start" -> cas1ApplicationStartUrl
          "/applications/{applicationId}" -> cas1Application?.uiUrl
          else -> error("Unexpected CAS1 application URL: ${s.expectedCas1Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)
      }
    }
  }

  @Nested
  inner class Cas2EligibilityScenarios {

    fun loadCas2EligibilityScenarios(): List<Cas2EligibilityScenario> {
      val rows = CsvReader().read("/cas2-eligibility-scenarios.csv")

      return rows.flatMapIndexed { idx, row ->

        val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()
        val tierScores = row["tierScore"].expandTierScore()

        try {
          accommodationTypes.flatMap { accommodationType ->
            tierScores.map { tierScore ->
              Cas2EligibilityScenario(
                testCaseId = row["testCaseId"]!!,
                description = row["description"],
                referenceDate = row["referenceDate"]!!.toLocalDate(),
                hasNextAccommodation = row["hasNextAccommodation"]!!,
                currentAccommodationType = accommodationType,
                currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
                tierScore = tierScore,
                expectedCas2Status = row["expectedCas2Status"]?.let { ServiceStatusNew.valueOf(it) },
                expectedCas2Url = row["expectedCas2Url"],
                u18 = row["u18"]?.toBoolean(),
                hasSexualOffenceRegistrations = row["hasSexualOffenceRegistrations"]!!.lowercase().toBooleanStrict(),
                expectedFailureReasons = row["expectedFailureReasons"]
                  ?.takeIf { it.isNotBlank() }
                  ?.split(",")
                  ?.map { FailureReason.valueOf(it.trim()) }
                  ?: emptyList(),
              )
            }
          }
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    fun loadCas2ApplicationScenarios(): List<Cas2ApplicationScenario> {
      val rows = CsvReader().read("/cas2-application-scenarios.csv")

      return rows.flatMapIndexed { idx, row ->

        val applicationStatuses = row["cas2ApplicationStatus"].expandCas2ApplicationStatus()

        try {
          applicationStatuses.map { applicationStatus ->
            Cas2ApplicationScenario(
              testCaseId = row["testCaseId"]!!,
              description = row["description"],
              referenceDate = row["referenceDate"]!!.toLocalDate(),
              currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
              isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
              submittedAt = row["submittedAt"]?.toLocalDate(),
              cas2ApplicationStatus = applicationStatus,
              expectedCas2Status = row["expectedCas2Status"]?.let { ServiceStatusNew.valueOf(it) },
              expectedCas2Url = row["expectedCas2Url"],
              markedAsArrived = row["markedAsArrived"]!!.toBoolean(),
            )
          }
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    private fun String?.expandCas2ApplicationStatus() = when (this) {
      "any" -> Cas2AssessmentStatus.entries.toList()
      null -> listOf(null)
      else -> listOf(Cas2AssessmentStatus.forValue(this))
    }

    private fun String?.expandTierScore() = when (this) {
      "A-G" -> listOf("A", "B", "C", "D", "E", "F", "G")
      "MISSING-OR-NOT-SUPERVISED" -> listOf("MISSING", "NOT_SUPERVISED")
      else -> listOf(this)
    }

    @Test
    fun `should calculate eligibility for cas2 for all scenarios`() {
      val scenarios = loadCas2EligibilityScenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val currentAccommodation = s.currentAccommodationType?.let {
          buildAccommodationSummaryDto(
            endDate = s.currentAccommodationEndDate,
            type = buildAccommodationTypeDto(
              code = s.currentAccommodationType,
            ),
          )
        }

        val nextAccommodations = if (s.hasNextAccommodation.toBoolean()) {
          listOf(buildAccommodationSummaryDto())
        } else {
          emptyList()
        }

        val aged18 = LocalDate.now(clock).minusYears(18)
        val under18 = LocalDate.now(clock).minusYears(18).plusDays(1)

        val data = buildDomainData(
          crn = s.testCaseId,
          currentAccommodation = currentAccommodation,
          nextAccommodations = nextAccommodations,
          cas2Application = null,
          currentAccommodationTypeEntity = s.currentAccommodationType?.let {
            buildAccommodationTypeEntity(
              code = it,
              isCas1 = it == "A02",
              isPrison = it == "HMP",
              isCas2 = it == "A10" || it == "A11",
              isCas3 = it == "A17",
            )
          },
          dob = if (s.u18 == null) {
            null
          } else if (s.u18) {
            under18
          } else {
            aged18
          },
          tierScore = s.tierScore,
          sexualOffenceRegistrations = if (s.hasSexualOffenceRegistrations) {
            buildSexualOffenceRegistrations(crn = s.testCaseId)
          } else {
            null
          },
        )

        val result = eligibilityService.evaluate(cas2Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas2Status}")
          .isEqualTo(s.expectedCas2Status)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS2)
        val expectedUrl = when (s.expectedCas2Url) {
          null -> null
          "/applications/start" -> "CAS2_APPLICATION_START_URL${s.testCaseId}"
          else -> error("Unexpected CAS2 eligibility URL: ${s.expectedCas2Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)

        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }

    @Test
    fun `should calculate application for cas2 for all scenarios`() {
      val scenarios = loadCas2ApplicationScenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val cas2Application = buildCas2Application(
          submittedApplication = if (s.submittedAt != null) {
            buildCas2SubmittedApplicationSummary(
              latestAssessmentStatus = s.cas2ApplicationStatus,
              markedArrivedDateTime = if (s.markedAsArrived) {
                s.referenceDate.atStartOfDay().toInstant(ZoneOffset.UTC)
              } else {
                null
              },
              submittedAt = s.submittedAt.atStartOfDay().atOffset(ZoneOffset.UTC),
              cancelledReason = if (s.cas2ApplicationStatus == Cas2AssessmentStatus.CANCELLED) {
                "cancelled reason"
              } else {
                null
              },
              offerDeclinedReason = if (s.cas2ApplicationStatus == Cas2AssessmentStatus.OFFER_DECLINED) {
                "offerDeclined reason"
              } else {
                null
              },
            )
          } else {
            null
          },
        )

        val data = buildDomainData(
          crn = s.testCaseId,
          currentAccommodation = buildAccommodationSummaryDto(
            endDate = s.currentAccommodationEndDate,
            type = buildAccommodationTypeDto(
              code = "A02",
            ),
          ),
          nextAccommodations = emptyList(),
          cas2Application = cas2Application,
          currentAccommodationTypeEntity = buildAccommodationTypeEntity(
            code = "A02",
            isCas1 = true,
            isCas2 = !s.isEligible,
          ),
          dob = LocalDate.now(clock).minusYears(18),
          tierScore = "A",
          sexualOffenceRegistrations = null,
        )

        val result = eligibilityService.evaluate(cas2Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas2Status}")
          .isEqualTo(s.expectedCas2Status)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS2)
        val expectedUrl = when (s.expectedCas2Url) {
          null -> null
          "/applications/start" -> "CAS2_APPLICATION_START_URL${s.testCaseId}"
          "/applications/{applicationId}" -> cas2Application.uiUrl
          else -> error("Unexpected CAS2 application URL: ${s.expectedCas2Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)
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
            expectedDtrStatus = row["expectedDtrStatus"]?.let { ServiceStatusNew.valueOf(it) },
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

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusWeeks(8)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.DTR)
        assertThat(result.url).isNull()
        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }
  }

  @Nested
  inner class Cas3EligibilityScenarios {

    fun loadCas3ApplicationScenarios(): List<Cas3ApplicationScenario> {
      val rows = CsvReader().read("/cas3-application-scenarios.csv")

      return rows.mapIndexed { idx, row ->
        try {
          Cas3ApplicationScenario(
            testCaseId = row["testCaseId"]!!,
            description = row["description"],
            referenceDate = row["referenceDate"]!!.toLocalDate(),
            currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
            expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatusNew.valueOf(it) },
            expectedCas3Url = row["expectedCas3Url"]?.takeIf { it.isNotBlank() },
            isEligible = row["isEligible"]!!.lowercase().toBooleanStrict(),
            isStarted = row["isStarted"]!!.lowercase().toBooleanStrict(),
            cas3ApplicationStatus = row["cas3ApplicationStatus"]?.let { Cas3ApplicationStatus.valueOf(it) },
            cas3AssessmentStatus = row["cas3AssessmentStatus"]?.let { Cas3AssessmentStatus.valueOf(it) },
            cas3BookingStatus = row["cas3BookingStatus"]?.let { Cas3BookingStatus.valueOf(it) },
          )
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    fun loadCas3EligibilityScenarios(): List<Cas3EligibilityScenario> {
      val rows = CsvReader().read("/cas3-eligibility-scenarios.csv")

      return rows.flatMapIndexed { idx, row ->
        try {
          val accommodationTypes = row["currentAccommodationType"].expandCurrentAccommodationType()
          val sexCodes = when (val sexCode = row["sexCode"]) {
            "notM" -> listOf(SexCode.N, SexCode.F, SexCode.NS)
            null -> listOf(null)
            else -> listOf(SexCode.valueOf(sexCode))
          }
          accommodationTypes.flatMap { accommodationType ->
            sexCodes.map { sexCode ->
              Cas3EligibilityScenario(
                testCaseId = row["testCaseId"]!!,
                description = row["description"],
                referenceDate = row["referenceDate"]!!.toLocalDate(),
                hasNextAccommodation = row["hasNextAccommodation"]!!,
                currentAccommodationType = accommodationType,
                currentAccommodationEndDate = row["currentAccommodationEndDate"]?.toLocalDate(),
                dtrSubmissionDate = row["dtrSubmissionDate"]?.toLocalDate(),
                crsStatus = row["crsStatus"]?.let { CrsReferralStatus.valueOf(it) },
                sexCode = sexCode,
                expectedCas3Status = row["expectedCas3Status"]?.let { ServiceStatusNew.valueOf(it) },
                expectedCas3Url = row["expectedCas3Url"]?.takeIf { it.isNotBlank() },
                expectedFailureReasons = row["expectedFailureReasons"]
                  ?.takeIf { it.isNotBlank() }
                  ?.split(",")
                  ?.map { FailureReason.valueOf(it.trim()) }
                  ?: emptyList(),
              )
            }
          }
        } catch (e: Exception) {
          throw IllegalStateException("Row $idx failed: $row", e)
        }
      }
    }

    @Test
    fun `should calculate application for cas3 for all scenarios`() {
      val scenarios = loadCas3ApplicationScenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val cas3Application = s.cas3ApplicationStatus?.let { applicationStatus ->
          buildCas3Application(
            applicationStatus = applicationStatus,
            submittedApplication = if (applicationStatus == Cas3ApplicationStatus.IN_PROGRESS) {
              null
            } else {
              buildCas3SubmittedApplication(
                assessmentStatus = s.cas3AssessmentStatus,
                assessmentRejectionReason = "Rejected".takeIf { s.cas3AssessmentStatus == Cas3AssessmentStatus.REJECTED },
                latestBooking = s.cas3BookingStatus?.let { buildCas3LatestBooking(status = it) },
              )
            },
          )
        }

        val dutyToRefer = buildDutyToReferDto(
          submission = buildDtrSubmission(submissionDate = s.referenceDate),
        )

        val currentAccommodation = buildAccommodationSummaryDto(
          endDate = s.currentAccommodationEndDate,
          type = buildAccommodationTypeDto(code = if (s.isEligible) "HMP" else "CAS3"),
        )

        val currentAccommodationTypeEntity = if (s.isEligible) {
          buildAccommodationTypeEntity(
            code = "HMP",
            isPrison = true,
          )
        } else {
          buildAccommodationTypeEntity(
            code = "CAS3",
            isCas3 = true,
          )
        }

        val commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
          sentAt = s.referenceDate.atStartOfDay().atOffset(ZoneOffset.UTC),
          status = if (s.isStarted) CrsReferralStatus.LIVE else CrsReferralStatus.WITHDRAWN,
        )

        val data = buildDomainData(
          crn = s.testCaseId,
          tierScore = null,
          sex = SexCode.M,
          currentAccommodation = currentAccommodation,
          nextAccommodations = emptyList(),
          cas3Application = cas3Application,
          dutyToRefer = dutyToRefer,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        )

        val result = eligibilityService.evaluate(cas3Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas3Status}")
          .isEqualTo(s.expectedCas3Status)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS3)

        val expectedUrl = when (s.expectedCas3Url) {
          null -> null
          "/referrals/start" -> cas3ReferralStartUrl
          "/referrals/{referralId}/full" -> cas3Application?.uiUrl
          else -> error("Unexpected CAS3 application URL: ${s.expectedCas3Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)
      }
    }

    @Test
    fun `should calculate eligibility for cas3 for all scenarios`() {
      val scenarios = loadCas3EligibilityScenarios()

      runScenarios(scenarios) { s ->

        clock.setNow(s.referenceDate)

        val dutyToRefer = s.dtrSubmissionDate?.let {
          buildDutyToReferDto(
            submission = buildDtrSubmission(submissionDate = it),
          )
        }

        val currentAccommodation = s.currentAccommodationType?.let {
          buildAccommodationSummaryDto(
            endDate = s.currentAccommodationEndDate,
            type = buildAccommodationTypeDto(code = it),
          )
        }

        val currentAccommodationTypeEntity = s.currentAccommodationType?.let {
          buildAccommodationTypeEntity(
            code = it,
            isPrison = it == "HMP",
            isCas1 = it == "A02",
            isCas2 = it == "A10" || it == "A11",
            isCas3 = it == "A17",
          )
        }

        val commissionedRehabilitativeServices = s.crsStatus?.let {
          buildCommissionedRehabilitativeServices(
            sentAt = s.referenceDate.atStartOfDay().atOffset(ZoneOffset.UTC),
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
          tierScore = null,
          sex = s.sexCode,
          currentAccommodation = currentAccommodation,
          nextAccommodations = nextAccommodations,
          cas3Application = null,
          dutyToRefer = dutyToRefer,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        )

        val result = eligibilityService.evaluate(cas3Tree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCas3Status}")
          .isEqualTo(s.expectedCas3Status)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusDays(UPCOMING_DAYS)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CAS3)

        val expectedUrl = when (s.expectedCas3Url) {
          null -> null
          "/referrals/start" -> cas3ReferralStartUrl
          else -> error("Unexpected CAS3 eligibility URL: ${s.expectedCas3Url}")
        }
        assertThat(result.url).isEqualTo(expectedUrl)
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
            currentAccommodationSettledType = row["currentAccommodationSettledType"]?.takeIf { it.isNotBlank() }?.let { AccommodationSettledType.valueOf(it) },
            crsStatus = row["crsStatus"]?.let { CrsReferralStatus.valueOf(it) },
            expectedCrsStatus = row["expectedCrsStatus"]?.let { ServiceStatusNew.valueOf(it) },
            expectedCrsUrl = row["expectedCrsUrl"]?.takeIf { it.isNotBlank() },
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

        val currentAccommodationTypeEntity = s.currentAccommodationSettledType?.let {
          buildAccommodationTypeEntity(settledType = it)
        }

        val data = buildDomainData(
          crn = s.testCaseId,
          sex = s.sex,
          currentAccommodation = currentAccommodation,
          nextAccommodations = nextAccommodations,
          commissionedRehabilitativeServices = commissionedRehabilitativeServices,
          currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        )

        val result = eligibilityService.evaluate(crsTree, data)

        assertThat(result.serviceStatus)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedCrsStatus}")
          .isEqualTo(s.expectedCrsStatus)

        assertThat(result.actionStartDate).isEqualTo(
          if (result.serviceStatus.isUpcoming) {
            s.currentAccommodationEndDate?.minusWeeks(12)
          } else {
            null
          },
        )
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.CRS)
        val expectedUrl = if (s.expectedCrsUrl == "CRS_UI_URL") crsUiUrl else null
        assertThat(result.url).isEqualTo(expectedUrl)
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
            expectedPaStatus = row["expectedPaStatus"]?.let { ServiceStatusNew.valueOf(it) },
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
            application = buildCas1ApplicationSummary(
              status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            ),
            requestForPlacement = buildCas1RequestForPlacementSummary(
              status = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
            ),
            placement = buildCas1PlacementSummary(
              status = Cas1PlacementStatus.UPCOMING,
            ),
          )
        } else {
          null
        }

        val cas3Application = if (s.isSubmittedCas3.toBoolean()) {
          buildCas3Application(
            applicationStatus = Cas3ApplicationStatus.SUBMITTED,
            submittedApplication = buildCas3SubmittedApplication(
              assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
              latestBooking = buildCas3LatestBooking(
                status = Cas3BookingStatus.CONFIRMED,
              ),
            ),
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
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Service Status: ${result.serviceStatus}, Expected Service Status: ${s.expectedPaStatus}")
          .isEqualTo(s.expectedPaStatus)

        assertThat(result.actionStartDate).isNull()
        assertThat(result.serviceStatus.service).isEqualTo(ServiceType.PA)
        assertThat(result.url).isNull()

        assertThat(result.failureReasons)
          .withFailMessage("${s.testCaseId} - ${s.description}, Actual Failure reasons: ${result.failureReasons}, Expected Failure reasons: ${s.expectedFailureReasons}")
          .containsExactlyInAnyOrderElementsOf(s.expectedFailureReasons)
      }
    }
  }

  private fun String?.expandCurrentAccommodationType(): List<String?> = when (this) {
    "A10-A11" -> listOf("A10", "A11")
    "notPrisonCas1OrCas2" -> "notPrisonOrCas1".expandCurrentAccommodationType()
      .filterNot { it == "A10" || it == "A11" }

    "notPrisonOrCas1" -> listOf(
      "A16",
      "A10",
      "A11",
      "A17",
      "A07B",
      "A07A",
      "A14",
      "A13",
      "A08A",
      "A08C",
      "A08",
      "A01A",
      "A15",
      "A12",
      "A01C",
      "A01D",
      "A04",
      "A03",
    )

    else -> listOf(this)
  }
}

data class Cas1EligibilityScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val sex: SexCode?,
  val tierScore: String?,
  val currentAccommodationType: String?,
  val currentAccommodationEndDate: LocalDate?,
  val expectedCas1Status: ServiceStatusNew?,
  val expectedCas1Url: String?,
  val expectedFailureReasons: List<FailureReason>,
)

data class Cas1ApplicationScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationEndDate: LocalDate?,
  val isEligible: Boolean,
  val cas1ApplicationStatus: Cas1ApplicationStatus?,
  val cas1RequestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val cas1PlacementStatus: Cas1PlacementStatus?,
  val expectedCas1Status: ServiceStatusNew?,
  val expectedCas1Url: String?,
)

data class Cas2EligibilityScenario(
  val testCaseId: String,
  val description: String?,
  val hasNextAccommodation: String,
  val tierScore: String?,
  val referenceDate: LocalDate,
  val currentAccommodationType: String?,
  val currentAccommodationEndDate: LocalDate?,
  val expectedCas2Status: ServiceStatusNew?,
  val expectedCas2Url: String?,
  val u18: Boolean?,
  val hasSexualOffenceRegistrations: Boolean,
  val expectedFailureReasons: List<FailureReason>,
)

data class Cas2ApplicationScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationEndDate: LocalDate?,
  val isEligible: Boolean,
  val submittedAt: LocalDate?,
  val cas2ApplicationStatus: Cas2AssessmentStatus?,
  val expectedCas2Status: ServiceStatusNew?,
  val expectedCas2Url: String?,
  val markedAsArrived: Boolean,
)

data class DtrScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationEndDate: LocalDate?,
  val hasNextAccommodation: String,
  val dtrStatus: DtrStatus?,
  val dtrSubmissionDate: LocalDate?,
  val expectedDtrStatus: ServiceStatusNew?,
  val expectedFailureReasons: List<FailureReason>,
)

data class Cas3EligibilityScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val hasNextAccommodation: String,
  val currentAccommodationType: String?,
  val currentAccommodationEndDate: LocalDate?,
  val dtrSubmissionDate: LocalDate?,
  val crsStatus: CrsReferralStatus?,
  val expectedCas3Status: ServiceStatusNew?,
  val sexCode: SexCode?,
  val expectedCas3Url: String?,
  val expectedFailureReasons: List<FailureReason>,
)

data class Cas3ApplicationScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val currentAccommodationEndDate: LocalDate?,
  val isEligible: Boolean,
  val isStarted: Boolean,
  val cas3ApplicationStatus: Cas3ApplicationStatus?,
  val cas3AssessmentStatus: Cas3AssessmentStatus?,
  val cas3BookingStatus: Cas3BookingStatus?,
  val expectedCas3Status: ServiceStatusNew?,
  val expectedCas3Url: String?,
)

data class CrsScenario(
  val testCaseId: String,
  val description: String?,
  val referenceDate: LocalDate,
  val sex: SexCode?,
  val hasNextAccommodation: String,
  val currentAccommodationEndDate: LocalDate?,
  val currentAccommodationSettledType: AccommodationSettledType?,
  val crsSubmissionDate: LocalDate?,
  val crsStatus: CrsReferralStatus?,
  val expectedCrsStatus: ServiceStatusNew?,
  val expectedCrsUrl: String?,
  val expectedFailureReasons: List<FailureReason>,
)

data class PaScenario(
  val testCaseId: String,
  val description: String?,
  val hasNextAccommodation: String,
  val isSubmittedCas1: String,
  val isSubmittedCas3: String,
  val expectedPaStatus: ServiceStatusNew?,
  val expectedFailureReasons: List<FailureReason>,
)

private const val UPCOMING_DAYS = 15L

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
