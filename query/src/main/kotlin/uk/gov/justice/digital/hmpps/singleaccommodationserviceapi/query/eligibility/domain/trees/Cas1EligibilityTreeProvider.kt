package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.ActivePlacementContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1RejectedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.DepartedPlacementContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.NoPlacementContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.NoRequestForPlacementContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ActivePlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1ActiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1NotSubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1RejectedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.MaleTierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotMaleTierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin28WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RequestForPlacementRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
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
  private val noRequestForPlacementContextUpdater: NoRequestForPlacementContextUpdater,
  private val activePlacementContextUpdater: ActivePlacementContextUpdater,
  private val departedPlacementContextUpdater: DepartedPlacementContextUpdater,
  private val activePlacement: ActivePlacementRuleSet,
  private val startContextUpdater: Cas1StartContextUpdater,
  private val noPlacementContextUpdater: NoPlacementContextUpdater,
  private val maleTierEligibleForCas1: MaleTierEligibleForCas1RuleSet,
  private val notMaleTierEligibleForCas1: NotMaleTierEligibleForCas1RuleSet,
  private val notMale: NotMaleRuleSet,
  private val currentAccommodationEligibleForCas1: CurrentAccommodationEligibleForCas1RuleSet,
  private val roshEligible: RoshEligibleRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS1)

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

    val activeNode = builder
      .ruleSet(activeReferral)
      .onPass(confirmed).applyContextUpdaterOnPass(noRequestForPlacementContextUpdater)
      .onFail(rejectedNode)
      .build()

    val requestForPlacementNode = builder
      .ruleSet(requestForPlacement)
      .onPass(confirmed).applyContextUpdaterOnPass(noPlacementContextUpdater)
      .onFail(activeNode)
      .build()

    val activePlacementNode = builder
      .ruleSet(activePlacement)
      .onPass(confirmed).applyContextUpdaterOnPass(activePlacementContextUpdater)
      .onFail(confirmed).applyContextUpdaterOnFailure(departedPlacementContextUpdater)
      .build()

    val placementNode = builder
      .ruleSet(placement)
      .onPass(activePlacementNode)
      .onFail(requestForPlacementNode)
      .build()

    val referralNode = builder
      .ruleSet(referral)
      .onPass(placementNode)
      .onFail(notMaleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin28Weeks)
      .onPass(referralNode)
      .onFail(notMaleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
