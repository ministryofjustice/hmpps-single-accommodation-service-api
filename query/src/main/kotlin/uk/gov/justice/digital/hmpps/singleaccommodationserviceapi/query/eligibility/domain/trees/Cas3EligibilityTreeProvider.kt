package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3CannotStartMaleContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3CannotStartNotMaleContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotStartedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotStartedNewContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3RejectedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3ActiveBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3ArrivedBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3AssessmentNotClosedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3ConfirmedBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3HasBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3NotArrivedBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3NotRejectedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3PresentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3ProvisionalBookingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationPrisonCas1OrCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrNotExpiredAndCrsLiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.HasRoshLevelRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LeavingWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas3EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val leavingWithin15DaysRuleSet: LeavingWithin15DaysRuleSet,
  private val cas3UpcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val cas3HasBookingRuleSet: Cas3HasBookingRuleSet,
  private val cas3SubmittedRuleSet: Cas3SubmittedRuleSet,
  private val cas3PresentRuleSet: Cas3PresentRuleSet,
  private val cas3NotRejectedRuleSet: Cas3NotRejectedRuleSet,
  private val cas3AssessmentNotClosedRuleSet: Cas3AssessmentNotClosedRuleSet,
  private val cas3NotStartedContextUpdater: Cas3NotStartedContextUpdater,
  private val cas3NotSubmittedContextUpdater: Cas3NotSubmittedContextUpdater,
  private val cas3RejectedContextUpdater: Cas3RejectedContextUpdater,
  private val cas3NotStartedNewContextUpdater: Cas3NotStartedNewContextUpdater,
  private val cas3ConfirmedBookingRuleSet: Cas3ConfirmedBookingRuleSet,
  private val cas3ArrivedBookingRuleSet: Cas3ArrivedBookingRuleSet,
  private val cas3ProvisionalBookingRuleSet: Cas3ProvisionalBookingRuleSet,
  private val cas3NotArrivedBookingRuleSet: Cas3NotArrivedBookingRuleSet,
  private val currentAccommodationPrisonCas1OrCas2RuleSet: CurrentAccommodationPrisonCas1OrCas2RuleSet,
  private val noNextAccommodationRuleSet: NoNextAccommodationRuleSet,
  private val dtrNotExpiredAndCrsLiveRuleSet: DtrNotExpiredAndCrsLiveRuleSet,
  private val cas3CannotStartNotMaleContextUpdater: Cas3CannotStartNotMaleContextUpdater,
  private val cas3CannotStartMaleContextUpdater: Cas3CannotStartMaleContextUpdater,
  private val hasRoshLevelRuleSet: HasRoshLevelRuleSet,
  private val cas3ActiveBookingRuleSet: Cas3ActiveBookingRuleSet,
  private val notMaleRuleSet: NotMaleRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Temporary accommodation",
    description = "Works out whether the person is eligible for CAS3 temporary accommodation and tracks the referral, assessment and bedspace booking status.",
    displayOrder = 3,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS3)
    val bookingConfirmed = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_BOOKING_CONFIRMED),
    )
    val arrived = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_ARRIVED),
    )
    val bedspaceOffered = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_BEDSPACE_OFFERED),
    )
    val notArrived = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_NOT_ARRIVED),
    )
    val bookingCancelled = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_BOOKING_CANCELLED),
    )
    val submitted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS3_SUBMITTED),
    )

    val leavingWithin15DaysRuleSetNode = builder
      .ruleSet(leavingWithin15DaysRuleSet)
      .onPass(confirmed)
      .onFail(confirmed).applyContextUpdaterOnFailure(cas3UpcomingContextUpdater)
      .build()

    val notMaleRuleSetNode = builder
      .ruleSet(notMaleRuleSet)
      .onPass(confirmed).applyContextUpdaterOnPass(cas3CannotStartNotMaleContextUpdater)
      .onFail(confirmed).applyContextUpdaterOnFailure(cas3CannotStartMaleContextUpdater)
      .build()

    val dtrNotExpiredAndCrsLiveRuleSetNode = builder
      .ruleSet(dtrNotExpiredAndCrsLiveRuleSet)
      .onPass(leavingWithin15DaysRuleSetNode)
      .onFail(notMaleRuleSetNode)
      .build()

    val hasRoshLevelRuleSetNode = builder
      .ruleSet(hasRoshLevelRuleSet)
      .onPass(dtrNotExpiredAndCrsLiveRuleSetNode)
      .onFail(notEligible)
      .build()

    val noNextAccommodationRuleSetNode = builder
      .ruleSet(noNextAccommodationRuleSet)
      .onPass(hasRoshLevelRuleSetNode)
      .onFail(notEligible)
      .build()

    val currentAccommodationPrisonCas1OrCas2RuleSetNode = builder
      .ruleSet(currentAccommodationPrisonCas1OrCas2RuleSet)
      .onPass(noNextAccommodationRuleSetNode)
      .onFail(notEligible)
      .build()

    val cas3NotArrivedBookingRuleSetNode = builder
      .ruleSet(cas3NotArrivedBookingRuleSet)
      .onPass(notArrived)
      .onFail(bookingCancelled)
      .build()

    val cas3ProvisionalBookingRuleSetNode = builder
      .ruleSet(cas3ProvisionalBookingRuleSet)
      .onPass(bedspaceOffered)
      .onFail(cas3NotArrivedBookingRuleSetNode)
      .build()

    val cas3ArrivedBookingRuleSetNode = builder
      .ruleSet(cas3ArrivedBookingRuleSet)
      .onPass(arrived)
      .onFail(cas3ProvisionalBookingRuleSetNode)
      .build()

    val cas3ConfirmedBookingRuleSetNode = builder
      .ruleSet(cas3ConfirmedBookingRuleSet)
      .onPass(bookingConfirmed)
      .onFail(cas3ArrivedBookingRuleSetNode)
      .build()

    val cas3PresentRuleSetNode = builder
      .ruleSet(cas3PresentRuleSet)
      .onPass(currentAccommodationPrisonCas1OrCas2RuleSetNode).applyContextUpdaterOnPass(cas3NotSubmittedContextUpdater)
      .onFail(currentAccommodationPrisonCas1OrCas2RuleSetNode).applyContextUpdaterOnFailure(cas3NotStartedContextUpdater)
      .build()

    val cas3SubmittedRuleSetNode = builder
      .ruleSet(cas3SubmittedRuleSet)
      .onPass(submitted)
      .onFail(cas3PresentRuleSetNode)
      .build()

    val cas3NotRejectedRuleSetNode = builder
      .ruleSet(cas3NotRejectedRuleSet)
      .onPass(cas3SubmittedRuleSetNode)
      .onFail(currentAccommodationPrisonCas1OrCas2RuleSetNode).applyContextUpdaterOnFailure(cas3RejectedContextUpdater)
      .build()

    val cas3AssessmentNotClosedRuleSetNode = builder
      .ruleSet(cas3AssessmentNotClosedRuleSet)
      .onPass(cas3NotRejectedRuleSetNode)
      .onFail(currentAccommodationPrisonCas1OrCas2RuleSetNode).applyContextUpdaterOnFailure(cas3NotStartedNewContextUpdater)
      .build()

    val cas3ActiveBookingRuleSetNode = builder
      .ruleSet(cas3ActiveBookingRuleSet)
      .onPass(cas3ConfirmedBookingRuleSetNode)
      .onFail(currentAccommodationPrisonCas1OrCas2RuleSetNode).applyContextUpdaterOnFailure(cas3NotStartedNewContextUpdater)
      .build()

    return builder
      .ruleSet(cas3HasBookingRuleSet)
      .onPass(cas3ActiveBookingRuleSetNode)
      .onFail(cas3AssessmentNotClosedRuleSetNode)
      .build()
  }
}
