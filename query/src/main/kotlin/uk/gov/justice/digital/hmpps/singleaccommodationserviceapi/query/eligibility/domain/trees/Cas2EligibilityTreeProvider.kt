package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2CancelledContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2NotStartedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2OfferDeclinedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2WithdrawnContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2ActiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2AssessedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2AwaitingArrivalRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2CancelledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2MarkedAsArrivedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2MoreInformationNeededRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OfferAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OfferDeclinedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OnWaitingListRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2PlaceOfferedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2PresentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseDateEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin15Days: ReleaseWithin15DaysRuleSet,
  private val upcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val activeAssessmentRuleSet: Cas2ActiveRuleSet,
  private val personEligible: PersonEligibleRuleSet,
  private val releaseDateEligible: ReleaseDateEligibleRuleSet,
  private val currentAccommodationEligible: CurrentAccommodationEligibleForCas2RuleSet,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
  private val roshEligible: RoshEligibleRuleSet,
  private val awaitingArrivalRuleSet: Cas2AwaitingArrivalRuleSet,
  private val markedAsArrivedRuleSet: Cas2MarkedAsArrivedRuleSet,
  private val placeOfferedRuleSet: Cas2PlaceOfferedRuleSet,
  private val offerAcceptedRuleSet: Cas2OfferAcceptedRuleSet,
  private val onWaitingListRuleSet: Cas2OnWaitingListRuleSet,
  private val moreInformationNeededRuleSet: Cas2MoreInformationNeededRuleSet,
  private val presentRuleSet: Cas2PresentRuleSet,
  private val assessedRuleSet: Cas2AssessedRuleSet,
  private val submittedRuleSet: Cas2SubmittedRuleSet,
  private val notStartedContextUpdater: Cas2NotStartedContextUpdater,
  private val notSubmittedContextUpdater: Cas2NotSubmittedContextUpdater,
  private val cancelledContextUpdater: Cas2CancelledContextUpdater,
  private val withdrawnContextUpdater: Cas2WithdrawnContextUpdater,
  private val offerDeclinedContextUpdater: Cas2OfferDeclinedContextUpdater,
  private val offerDeclinedRuleSet: Cas2OfferDeclinedRuleSet,
  private val cancelledRuleSet: Cas2CancelledRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS2)
    val placeOffered = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_PLACE_OFFERED),
    )
    val awaitingDecision = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_AWAITING_DECISION),
    )
    val offerAccepted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_OFFER_ACCEPTED),
    )
    val onWaitingList = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_ON_WAITING_LIST),
    )
    val moreInformationNeeded = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED),
    )
    val awaitingArrival = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_AWAITING_ARRIVAL),
    )
    val arrived = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_ARRIVED),
    )
    val submitted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS2_SUBMITTED),
    )
    val roshEligibleNode = builder
      .ruleSet(roshEligible)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val noNextAccommodationNode = builder
      .ruleSet(noNextAccommodation)
      .onPass(roshEligibleNode)
      .onFail(notEligible)
      .build()

    val currentAccommodationEligibleNode = builder
      .ruleSet(currentAccommodationEligible)
      .onPass(noNextAccommodationNode)
      .onFail(notEligible)
      .build()

    val releaseDateEligibleNode = builder
      .ruleSet(releaseDateEligible)
      .onPass(currentAccommodationEligibleNode)
      .onFail(notEligible)
      .build()

    val personEligibleNode = builder
      .ruleSet(personEligible)
      .onPass(releaseDateEligibleNode)
      .onFail(notEligible)
      .build()

    val moreInformationNeededRuleSetNode = builder
      .ruleSet(moreInformationNeededRuleSet)
      .onPass(moreInformationNeeded)
      .onFail(awaitingDecision)
      .build()

    val onWaitingListRuleSetNode = builder
      .ruleSet(onWaitingListRuleSet)
      .onPass(onWaitingList)
      .onFail(moreInformationNeededRuleSetNode)
      .build()

    val placeOfferedRuleSetNode = builder
      .ruleSet(placeOfferedRuleSet)
      .onPass(placeOffered)
      .onFail(onWaitingListRuleSetNode)
      .build()

    val offerAcceptedRuleSetNode = builder
      .ruleSet(offerAcceptedRuleSet)
      .onPass(offerAccepted)
      .onFail(placeOfferedRuleSetNode)
      .build()

    val awaitingArrivalRuleSetNode = builder
      .ruleSet(awaitingArrivalRuleSet)
      .onPass(awaitingArrival)
      .onFail(offerAcceptedRuleSetNode)
      .build()

    val markedAsArrivedRuleSetNode = builder
      .ruleSet(markedAsArrivedRuleSet)
      .onPass(arrived)
      .onFail(awaitingArrivalRuleSetNode)
      .build()

    val cancelledRuleSetNode = builder
      .ruleSet(cancelledRuleSet)
      .onPass(personEligibleNode).applyContextUpdaterOnPass(cancelledContextUpdater)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(withdrawnContextUpdater)
      .build()

    val offerDeclinedRuleSetNode = builder
      .ruleSet(offerDeclinedRuleSet)
      .onPass(personEligibleNode).applyContextUpdaterOnPass(offerDeclinedContextUpdater)
      .onFail(cancelledRuleSetNode)
      .build()

    val activeAssessmentNode = builder
      .ruleSet(activeAssessmentRuleSet)
      .onPass(markedAsArrivedRuleSetNode)
      .onFail(offerDeclinedRuleSetNode)
      .build()

    val assessedRuleSetNode = builder
      .ruleSet(assessedRuleSet)
      .onPass(activeAssessmentNode)
      .onFail(submitted)
      .build()

    val submittedRuleSetNode = builder
      .ruleSet(submittedRuleSet)
      .onPass(assessedRuleSetNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(notSubmittedContextUpdater)
      .build()

    val presentRuleSetNode = builder
      .ruleSet(presentRuleSet)
      .onPass(submittedRuleSetNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(notStartedContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin15Days)
      .onPass(presentRuleSetNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
