package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1RejectedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ActivePlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ActiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotSubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1RejectedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.MaleTierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.MoreInfoNotRequestedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotArrivedPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotDepartedPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleTierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotRejectedRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotWithdrawnRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin28WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RequestForPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.SubmittedRequestRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.UpcomingPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1DomainData

@Component
class Cas1EligibilityTreeProvider(
  engine: RulesEngine,
  private val releaseWithin28Weeks: ReleaseWithin28WeeksRuleSet,
  private val upcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val referral: Cas1RuleSet,
  private val notSubmittedReferral: Cas1NotSubmittedRuleSet,
  private val notSubmittedContextUpdater: Cas1NotSubmittedContextUpdater,
  private val activeReferral: Cas1ActiveRuleSet,
  private val rejectedContextUpdater: Cas1RejectedContextUpdater,
  private val rejected: Cas1RejectedRuleSet,
  private val placement: PlacementRuleSet,
  private val requestForPlacement: RequestForPlacementRuleSet,
  private val activePlacement: ActivePlacementRuleSet,
  private val notDepartedPlacement: NotDepartedPlacementRuleSet,
  private val upcomingPlacement: UpcomingPlacementRuleSet,
  private val notArrivedPlacement: NotArrivedPlacementRuleSet,
  private val startContextUpdater: Cas1StartContextUpdater,
  private val maleTierEligibleForCas1: MaleTierEligibleForCas1RuleSet,
  private val notMaleTierEligibleForCas1: NotMaleTierEligibleForCas1RuleSet,
  private val notMale: NotMaleRuleSet,
  private val currentAccommodationEligibleForCas1: CurrentAccommodationEligibleForCas1RuleSet,
  private val roshEligible: RoshEligibleRuleSet,
  private val notRejectedRequest: NotRejectedRequestRuleSet,
  private val notWithdrawnRequest: NotWithdrawnRequestRuleSet,
  private val submittedRequest: SubmittedRequestRuleSet,
  private val moreInfoNotRequested: MoreInfoNotRequestedRuleSet,
  private val submitted: Cas1SubmittedRuleSet,

) : EligibilityTreeProvider<Cas1DomainData> {
  private val builder = DecisionTreeBuilder<Cas1DomainData>(engine)

  private val tree: DecisionNode<Cas1DomainData> by lazy { build() }

  override fun tree(): DecisionNode<Cas1DomainData> = tree

  override fun initialContext(data: Cas1DomainData): EvaluationContext<Cas1DomainData> = EvaluationContext(
    data = data,
    currentResult = null,
  )

  override fun applicationUrl(data: Cas1DomainData): String? = data.cas1Application?.application?.uiUrl

  private fun build(): DecisionNode<Cas1DomainData> {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS1)
    val placementBooked = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED),
    )
    val arrived = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_ARRIVED),
    )
    val placementCancelled = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_CANCELLED),
    )
    val placementRequestNotStarted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED),
    )
    val notArrived = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_NOT_ARRIVED),
    )
    val rejectedRequest = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_REJECTED),
    )
    val withdrawnRequest = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_WITHDRAWN),
    )
    val placementRequestSubmitted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_SUBMITTED),
    )
    val placementRequestNotSubmitted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED),
    )
    val applicationSubmitted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_SUBMITTED),
    )
    val infoRequested = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS1_INFO_REQUESTED),
    )
    val roshEligibleNode = builder
      .ruleSet(roshEligible)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val currentAccommodationEligibleForCas1Node = builder
      .ruleSet(currentAccommodationEligibleForCas1)
      .onPass(roshEligibleNode)
      .onFail(notEligible)
      .build()

    val maleTierEligibleForCas1Node = builder
      .ruleSet(maleTierEligibleForCas1)
      .onPass(currentAccommodationEligibleForCas1Node)
      .onFail(notEligible)
      .build()

    val notMaleTierEligibleForCas1Node = builder
      .ruleSet(notMaleTierEligibleForCas1)
      .onPass(currentAccommodationEligibleForCas1Node)
      .onFail(notEligible)
      .build()

    val notMaleNode = builder
      .ruleSet(notMale)
      .onPass(notMaleTierEligibleForCas1Node)
      .onFail(maleTierEligibleForCas1Node)
      .build()

    val notSubmittedNode = builder
      .ruleSet(notSubmittedReferral)
      .onPass(notMaleNode).applyContextUpdaterOnPass(notSubmittedContextUpdater)
      .onFail(notMaleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    val rejectedNode = builder
      .ruleSet(rejected)
      .onPass(notMaleNode).applyContextUpdaterOnPass(rejectedContextUpdater)
      .onFail(notSubmittedNode)
      .build()

    val moreInfoNotRequestedNode = builder
      .ruleSet(moreInfoNotRequested)
      .onPass(applicationSubmitted)
      .onFail(infoRequested)
      .build()

    val submittedNode = builder
      .ruleSet(submitted)
      .onPass(moreInfoNotRequestedNode)
      .onFail(placementRequestNotSubmitted)
      .build()

    val activeNode = builder
      .ruleSet(activeReferral)
      .onPass(submittedNode)
      .onFail(rejectedNode)
      .build()

    val submittedRequestNode = builder
      .ruleSet(submittedRequest)
      .onPass(placementRequestSubmitted)
      .onFail(placementRequestNotSubmitted)
      .build()

    val notWithdrawnRequestNode = builder
      .ruleSet(notWithdrawnRequest)
      .onPass(submittedRequestNode)
      .onFail(withdrawnRequest)
      .build()

    val notRejectedRequestNode = builder
      .ruleSet(notRejectedRequest)
      .onPass(notWithdrawnRequestNode)
      .onFail(rejectedRequest)
      .build()

    val requestForPlacementNode = builder
      .ruleSet(requestForPlacement)
      .onPass(notRejectedRequestNode)
      .onFail(activeNode)
      .build()

    val notArrivedPlacementNode = builder
      .ruleSet(notArrivedPlacement)
      .onPass(notArrived)
      .onFail(placementCancelled)
      .build()

    val notDepartedPlacementNode = builder
      .ruleSet(notDepartedPlacement)
      .onPass(notArrivedPlacementNode)
      .onFail(placementRequestNotStarted)
      .build()

    val placementNode = builder
      .ruleSet(placement)
      .onPass(notDepartedPlacementNode)
      .onFail(requestForPlacementNode)
      .build()

    val referralNode = builder
      .ruleSet(referral)
      .onPass(placementNode)
      .onFail(notMaleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    val upcomingPlacementNode = builder
      .ruleSet(upcomingPlacement)
      .onPass(placementBooked)
      .onFail(arrived)
      .build()

    val activePlacementNode = builder
      .ruleSet(activePlacement)
      .onPass(upcomingPlacementNode)
      .onFail(referralNode)
      .build()

    return builder
      .ruleSet(releaseWithin28Weeks)
      .onPass(activePlacementNode)
      .onFail(notMaleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
