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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.CurrentAccommodationEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.TierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinTwentyEightWeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.rules.ReleaseOutsideTwentyEightWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.ReleaseDateEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.HasSexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.InPrisonNoReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.WrongTierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible.CurrentAccommodationEligibleForCas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible.rules.WrongCurrentAccommodationForCas3TypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3CannotStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3ReferralNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.NoRoshLevelRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.ReleaseOutside15DaysRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible.NotSettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible.rules.IsSettledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.CrsStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.LiveCrsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.rules.NoLiveCrsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.ReleaseWithin12WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.rules.ReleaseOutside12WeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.DtrActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.DtrRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrWithdrawnRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.ReleaseWithinEightWeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutsideEightWeeksRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.NoActiveCas1OrCas3ReferralsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas1ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.rules.Cas3ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.HasNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.rules.NoNextAccommodationRule
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
  var cas1CompletionContextUpdater = Cas1ActiveContextUpdater()
  var cas1SuitabilityRuleSet = Cas1RuleSet(Cas1NotPresentRule(), Cas1NotActiveRule(), Cas1ExpiredRule())
  var tierEligibleForCas1RuleSet = TierEligibleForCas1RuleSet(
    MaleRiskEligibilityRule(),
    NonMaleRiskEligibilityRule(),
  )
  val roshEligibleRuleSet = RoshEligibleRuleSet(
    NoRoshLevelRule(),
  )
  var currentAccommodationEligibleForCas1RuleSet = CurrentAccommodationEligibleForCas1RuleSet(
    CurrentAccommodationIsPrisonOrCas1Rule(),
  )
  val cas1UpcomingContextUpdater = Cas1UpcomingContextUpdater()
  var cas1UpcomingRuleSet = ReleaseWithinTwentyEightWeeksRuleSet(ReleaseOutsideTwentyEightWeeksRule(clock))
  val cas1SuitabilityContextUpdater = Cas1StartContextUpdater()

  // CAS2
  var cas2ApplicationStartUrl = "CAS2_APPLICATION_START_URL#crn"
  var cas2CompletionContextUpdater = Cas2ActiveContextUpdater(sentryService)
  var cas2RuleSet = Cas2RuleSet(Cas2NotSubmittedRule(), Cas2NotActiveRule())
  var personEligibleRuleSet = PersonEligibleRuleSet(
    Under18Rule(clock),
    WrongTierRule(),
    HasSexualOffenceRegistrationsRule(),
  )
  var releaseDateEligibleRuleSet = ReleaseDateEligibleRuleSet(
    InPrisonNoReleaseRule(),
  )
  var currentAccommodationEligibleForCas2RuleSet = CurrentAccommodationEligibleForCas2RuleSet(
    CurrentAccommodationTypeIsCas3Rule(),
    CurrentAccommodationTypeIsCas2Rule(),
    CurrentAccommodationTypeIsCommunityRule(),
  )
  var noNextAccommodationRuleSet = NoNextAccommodationRuleSet(
    HasNextAccommodationRule(),
  )
  val cas2UpcomingContextUpdater = Cas2UpcomingContextUpdater()
  var releaseWithin15DaysRuleSet = ReleaseWithin15DaysRuleSet(ReleaseOutside15DaysRule(clock))
  val cas2SuitabilityContextUpdater = Cas2StartContextUpdater(sentryService)

  // CAS3
  var cas3ReferralStartUrl = "CAS3_REFERRAL_START_URL"
  var cas3UpcomingContextUpdater = Cas3UpcomingContextUpdater()
  var cas3StartContextUpdater = Cas3StartContextUpdater()
  var cas3ActiveContextUpdater = Cas3ActiveContextUpdater()
  var cas3RuleSet = Cas3RuleSet(
    Cas3ReferralNotActiveRule(),
    Cas3NotPresentRule(),
    Cas3BookingNotActiveRule(),
    Cas3AssessmentNotActiveRule(),
  )
  var currentAccommodationEligibleForCas3RuleSet = CurrentAccommodationEligibleForCas3RuleSet(
    WrongCurrentAccommodationForCas3TypeRule(),
  )

  var cas3PrerequisiteRuleSet = Cas3PrerequisiteRuleSet(
    DtrExpiredRule(clock),
    CrsSubmittedRuleMale(),
    CrsSubmittedRuleNonMale(),
  )
  var cas3CannotStartContextUpdater = Cas3CannotStartContextUpdater()

  // DTR
  var releaseWithinEightWeeksRuleSet = ReleaseWithinEightWeeksRuleSet(ReleaseOutsideEightWeeksRule(clock))
  var dtrUpcomingContextUpdater = DtrUpcomingContextUpdater()
  var dtrRuleSet = DtrRuleSet(
    DtrNotPresentRule(),
    DtrWithdrawnRule(),
    DtrExpiredRule(clock),
  )
  var dtrActiveContextUpdater = DtrActiveContextUpdater()

  // CRS
  var crsUiUrl = "CRS_UI_URL"
  var notSettledRuleSet = NotSettledRuleSet(
    IsSettledRule(),
  )

  var liveCrsRuleSet = LiveCrsRuleSet(
    NoLiveCrsRule(),
  )
  var crsStartContextUpdater = CrsStartContextUpdater(crsUiUrl)
  var releaseOutside12WeeksRuleSet = ReleaseWithin12WeeksRuleSet(ReleaseOutside12WeeksRule(clock))
  var crsUpcomingContextUpdater = CrsUpcomingContextUpdater()

  // PA
  var noActiveCas1OrCas3ReferralsRuleSet = NoActiveCas1OrCas3ReferralsRuleSet(
    Cas1ActiveRule(),
    Cas3ActiveRule(),
  )
  var hasNextAccommodationRuleSet = HasNextAccommodationRuleSet(
    NoNextAccommodationRule(),
  )

  private val builder = DecisionTreeBuilder(rulesEngine)

  private val cas1Tree = Cas1EligibilityTreeProvider(
    builder = builder,
    releaseWithinTwentyEightWeeks = cas1UpcomingRuleSet,
    upcomingContextUpdater = cas1UpcomingContextUpdater,
    referral = cas1SuitabilityRuleSet,
    startContextUpdater = cas1SuitabilityContextUpdater,
    activeContextUpdater = cas1CompletionContextUpdater,
    tierEligibleForCas1 = tierEligibleForCas1RuleSet,
    currentAccommodationEligibleForCas1 = currentAccommodationEligibleForCas1RuleSet,
    roshEligible = roshEligibleRuleSet,
  )

  private val cas2Tree = Cas2EligibilityTreeProvider(
    builder = builder,
    upcomingContextUpdater = cas2UpcomingContextUpdater,
    referral = cas2RuleSet,
    startContextUpdater = cas2SuitabilityContextUpdater,
    activeContextUpdater = cas2CompletionContextUpdater,
    releaseWithin15Days = releaseWithin15DaysRuleSet,
    personEligible = personEligibleRuleSet,
    releaseDateEligible = releaseDateEligibleRuleSet,
    currentAccommodationEligible = currentAccommodationEligibleForCas2RuleSet,
    noNextAccommodation = noNextAccommodationRuleSet,
    roshEligible = roshEligibleRuleSet,
  )

  private val cas3Tree = Cas3EligibilityTreeProvider(
    builder = builder,
    upcomingContextUpdater = cas3UpcomingContextUpdater,
    referral = cas3RuleSet,
    startContextUpdater = cas3StartContextUpdater,
    activeContextUpdater = cas3ActiveContextUpdater,
    prerequisite = cas3PrerequisiteRuleSet,
    releaseWithin15DaysRuleSet = releaseWithin15DaysRuleSet,
    currentAccommodationEligible = currentAccommodationEligibleForCas3RuleSet,
    noNextAccommodation = noNextAccommodationRuleSet,
    cannotStartContextUpdater = cas3CannotStartContextUpdater,
    roshEligible = roshEligibleRuleSet,
  )

  private val dtrTree = DtrEligibilityTreeProvider(
    builder = builder,
    upcomingContextUpdater = dtrUpcomingContextUpdater,
    releaseWithinEightWeeks = releaseWithinEightWeeksRuleSet,
    referral = dtrRuleSet,
    activeContextUpdater = dtrActiveContextUpdater,
    noNextAccommodation = noNextAccommodationRuleSet,
  )

  private val crsTree = CrsEligibilityTreeProvider(
    builder = builder,
    upcoming = releaseOutside12WeeksRuleSet,
    upcomingContextUpdater = crsUpcomingContextUpdater,
    notSettledRuleSet = notSettledRuleSet,
    noNextAccommodation = noNextAccommodationRuleSet,
    liveCrs = liveCrsRuleSet,
    startContextUpdater = crsStartContextUpdater,
    crsUiBaseUrl = crsUiUrl,
  )

  private val paTree = PaEligibilityTreeProvider(
    builder = builder,
    noActiveCas1OrCas3Referrals = noActiveCas1OrCas3ReferralsRuleSet,
    hasNextAccommodation = hasNextAccommodationRuleSet,
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
      val roshLevel = buildRoshLevel()
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
          case = buildCase(crn = crn, roshLevel = roshLevel),
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
        roshLevel = roshLevel,
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
        case = null,
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
          case = null,
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
              riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
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
          roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
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
          roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
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
                riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
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
          roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
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
          roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
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
                riskLevel = row["RiskLevel"]?.let { RiskLevel.valueOf(it) },
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
          roshLevel = buildRoshLevel(code = RiskLevel.LOW.code),
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
          roshLevel = s.riskLevel?.let { buildRoshLevel(code = it.code) },
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
  val riskLevel: RiskLevel?,
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
  val riskLevel: RiskLevel?,
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
  val riskLevel: RiskLevel?,
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
