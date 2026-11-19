package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1RejectedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ActivePlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ApplicationActiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ApplicationNotSubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ApplicationPresentAndNotExpiredRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ApplicationRejectedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1MoreInfoNotRequestedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotArrivedPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotCancelledPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotDepartedPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotPendingPlacementRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotRejectedRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotWithdrawnRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1PlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1RequestForPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1SubmittedRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationPrisonOrCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.HasRoshLevelRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LeavingWithin28WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.TierAToCRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.TierAToDRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val leavingWithin28WeeksRuleSet: LeavingWithin28WeeksRuleSet,
  private val cas1UpcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val cas1ApplicationPresentAndNotExpiredRuleSet: Cas1ApplicationPresentAndNotExpiredRuleSet,
  private val cas1ApplicationNotSubmittedRuleSet: Cas1ApplicationNotSubmittedRuleSet,
  private val cas1NotSubmittedContextUpdater: Cas1NotSubmittedContextUpdater,
  private val cas1ApplicationActiveRuleSet: Cas1ApplicationActiveRuleSet,
  private val cas1RejectedContextUpdater: Cas1RejectedContextUpdater,
  private val cas1ApplicationRejectedRuleSet: Cas1ApplicationRejectedRuleSet,
  private val cas1PlacementRuleSet: Cas1PlacementRuleSet,
  private val cas1RequestForPlacementRuleSet: Cas1RequestForPlacementRuleSet,
  private val cas1ActivePlacementRuleSet: Cas1ActivePlacementRuleSet,
  private val cas1NotDepartedPlacementRuleSet: Cas1NotDepartedPlacementRuleSet,
  private val cas1NotArrivedPlacementRuleSet: Cas1NotArrivedPlacementRuleSet,
  private val cas1NotCancelledPlacementRuleSet: Cas1NotCancelledPlacementRuleSet,
  private val cas1StartContextUpdater: Cas1StartContextUpdater,
  private val tierAToCRuleSet: TierAToCRuleSet,
  private val tierAToDRuleSet: TierAToDRuleSet,
  private val notMaleRuleSet: NotMaleRuleSet,
  private val currentAccommodationPrisonOrCas1RuleSet: CurrentAccommodationPrisonOrCas1RuleSet,
  private val hasRoshLevelRuleSet: HasRoshLevelRuleSet,
  private val cas1NotRejectedRequestRuleSet: Cas1NotRejectedRequestRuleSet,
  private val cas1NotWithdrawnRequestRuleSet: Cas1NotWithdrawnRequestRuleSet,
  private val cas1SubmittedRequestRuleSet: Cas1SubmittedRequestRuleSet,
  private val cas1MoreInfoNotRequestedRuleSet: Cas1MoreInfoNotRequestedRuleSet,
  private val cas1NotPendingPlacementRequestRuleSet: Cas1NotPendingPlacementRequestRuleSet,

) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Approved Premises",
    description = "Works out whether the person is eligible for an Approved Premises (CAS1) placement and where they are in the process, from application and assessment through to request for placement and placement status.",
    displayOrder = 1,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS1)
    val placementBooked = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_BOOKED),
    )
    val arrived = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_ARRIVED),
    )
    val placementCancelled = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_CANCELLED),
    )
    val placementRequestNotStarted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_REQUEST_NOT_STARTED),
    )
    val notArrived = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_NOT_ARRIVED),
    )
    val rejectedRequest = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_REQUEST_REJECTED),
    )
    val withdrawnRequest = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_REQUEST_WITHDRAWN),
    )
    val placementRequestSubmitted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_REQUEST_SUBMITTED),
    )
    val placementRequestNotSubmitted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED),
    )
    val applicationSubmitted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_SUBMITTED),
    )
    val infoRequested = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS1_INFO_REQUESTED),
    )
    val hasRoshLevelRuleSetNode = builder
      .ruleSet(hasRoshLevelRuleSet)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val currentAccommodationPrisonOrCas1RuleSetNode = builder
      .ruleSet(currentAccommodationPrisonOrCas1RuleSet)
      .onPass(hasRoshLevelRuleSetNode)
      .onFail(notEligible)
      .build()

    val tierAToCRuleSetNode = builder
      .ruleSet(tierAToCRuleSet)
      .onPass(currentAccommodationPrisonOrCas1RuleSetNode)
      .onFail(notEligible)
      .build()

    val tierAToDRuleSetNode = builder
      .ruleSet(tierAToDRuleSet)
      .onPass(currentAccommodationPrisonOrCas1RuleSetNode)
      .onFail(notEligible)
      .build()

    val notMaleRuleSetNode = builder
      .ruleSet(notMaleRuleSet)
      .onPass(tierAToDRuleSetNode)
      .onFail(tierAToCRuleSetNode)
      .build()

    val cas1ApplicationNotSubmittedRuleSetNode = builder
      .ruleSet(cas1ApplicationNotSubmittedRuleSet)
      .onPass(notMaleRuleSetNode).applyContextUpdaterOnPass(cas1NotSubmittedContextUpdater)
      .onFail(notMaleRuleSetNode).applyContextUpdaterOnFailure(cas1StartContextUpdater)
      .build()

    val cas1ApplicationRejectedRuleSetNode = builder
      .ruleSet(cas1ApplicationRejectedRuleSet)
      .onPass(notMaleRuleSetNode).applyContextUpdaterOnPass(cas1RejectedContextUpdater)
      .onFail(cas1ApplicationNotSubmittedRuleSetNode)
      .build()

    val cas1MoreInfoNotRequestedRuleSetNode = builder
      .ruleSet(cas1MoreInfoNotRequestedRuleSet)
      .onPass(applicationSubmitted)
      .onFail(infoRequested)
      .build()

    val cas1NotPendingPlacementRequestRuleSetNode = builder
      .ruleSet(cas1NotPendingPlacementRequestRuleSet)
      .onPass(cas1MoreInfoNotRequestedRuleSetNode)
      .onFail(placementRequestNotSubmitted)
      .build()

    val cas1ApplicationActiveRuleSetNode = builder
      .ruleSet(cas1ApplicationActiveRuleSet)
      .onPass(cas1NotPendingPlacementRequestRuleSetNode)
      .onFail(cas1ApplicationRejectedRuleSetNode)
      .build()

    val cas1SubmittedRequestRuleSetNode = builder
      .ruleSet(cas1SubmittedRequestRuleSet)
      .onPass(placementRequestSubmitted)
      .onFail(placementRequestNotSubmitted)
      .build()

    val cas1NotWithdrawnRequestRuleSetNode = builder
      .ruleSet(cas1NotWithdrawnRequestRuleSet)
      .onPass(cas1SubmittedRequestRuleSetNode)
      .onFail(withdrawnRequest)
      .build()

    val cas1NotRejectedRequestRuleSetNode = builder
      .ruleSet(cas1NotRejectedRequestRuleSet)
      .onPass(cas1NotWithdrawnRequestRuleSetNode)
      .onFail(rejectedRequest)
      .build()

    val cas1RequestForPlacementRuleSetNode = builder
      .ruleSet(cas1RequestForPlacementRuleSet)
      .onPass(cas1NotRejectedRequestRuleSetNode)
      .onFail(cas1ApplicationActiveRuleSetNode)
      .build()

    val cas1NotCancelledPlacementRuleSetNode = builder
      .ruleSet(cas1NotCancelledPlacementRuleSet)
      .onPass(notArrived)
      .onFail(placementCancelled)
      .build()

    val cas1NotDepartedPlacementRuleSetNode = builder
      .ruleSet(cas1NotDepartedPlacementRuleSet)
      .onPass(cas1NotCancelledPlacementRuleSetNode)
      .onFail(placementRequestNotStarted)
      .build()

    val cas1PlacementRuleSetNode = builder
      .ruleSet(cas1PlacementRuleSet)
      .onPass(cas1NotDepartedPlacementRuleSetNode)
      .onFail(cas1RequestForPlacementRuleSetNode)
      .build()

    val cas1ApplicationPresentAndNotExpiredRuleSetNode = builder
      .ruleSet(cas1ApplicationPresentAndNotExpiredRuleSet)
      .onPass(cas1PlacementRuleSetNode)
      .onFail(notMaleRuleSetNode).applyContextUpdaterOnFailure(cas1StartContextUpdater)
      .build()

    val cas1NotArrivedPlacementRuleSetNode = builder
      .ruleSet(cas1NotArrivedPlacementRuleSet)
      .onPass(placementBooked)
      .onFail(arrived)
      .build()

    val cas1ActivePlacementRuleSetNode = builder
      .ruleSet(cas1ActivePlacementRuleSet)
      .onPass(cas1NotArrivedPlacementRuleSetNode)
      .onFail(cas1ApplicationPresentAndNotExpiredRuleSetNode)
      .build()

    return builder
      .ruleSet(leavingWithin28WeeksRuleSet)
      .onPass(cas1ActivePlacementRuleSetNode)
      .onFail(notMaleRuleSetNode).applyContextUpdaterOnFailure(cas1UpcomingContextUpdater)
      .build()
  }
}
