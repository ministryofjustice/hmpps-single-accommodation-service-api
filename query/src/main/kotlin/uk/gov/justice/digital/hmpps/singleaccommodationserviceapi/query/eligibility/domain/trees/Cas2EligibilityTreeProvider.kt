package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2MoreInformationRequestedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OfferAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OfferDeclinedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2OnWaitingListRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2PlaceOfferedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2PresentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationNotCas2Cas3OrCommunityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.HasRoshLevelRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LeavingWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotInPrisonWithoutReleaseDateRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val leavingWithin15DaysRuleSet: LeavingWithin15DaysRuleSet,
  private val cas2UpcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val cas2ActiveRuleSet: Cas2ActiveRuleSet,
  private val personEligibleRuleSet: PersonEligibleRuleSet,
  private val notInPrisonWithoutReleaseDateRuleSet: NotInPrisonWithoutReleaseDateRuleSet,
  private val currentAccommodationNotCas2Cas3OrCommunityRuleSet: CurrentAccommodationNotCas2Cas3OrCommunityRuleSet,
  private val noNextAccommodationRuleSet: NoNextAccommodationRuleSet,
  private val hasRoshLevelRuleSet: HasRoshLevelRuleSet,
  private val cas2AwaitingArrivalRuleSet: Cas2AwaitingArrivalRuleSet,
  private val cas2MarkedAsArrivedRuleSet: Cas2MarkedAsArrivedRuleSet,
  private val cas2PlaceOfferedRuleSet: Cas2PlaceOfferedRuleSet,
  private val cas2OfferAcceptedRuleSet: Cas2OfferAcceptedRuleSet,
  private val cas2OnWaitingListRuleSet: Cas2OnWaitingListRuleSet,
  private val cas2MoreInformationRequestedRuleSet: Cas2MoreInformationRequestedRuleSet,
  private val cas2PresentRuleSet: Cas2PresentRuleSet,
  private val cas2AssessedRuleSet: Cas2AssessedRuleSet,
  private val cas2SubmittedRuleSet: Cas2SubmittedRuleSet,
  private val cas2NotStartedContextUpdater: Cas2NotStartedContextUpdater,
  private val cas2NotSubmittedContextUpdater: Cas2NotSubmittedContextUpdater,
  private val cas2CancelledContextUpdater: Cas2CancelledContextUpdater,
  private val cas2WithdrawnContextUpdater: Cas2WithdrawnContextUpdater,
  private val cas2OfferDeclinedContextUpdater: Cas2OfferDeclinedContextUpdater,
  private val cas2OfferDeclinedRuleSet: Cas2OfferDeclinedRuleSet,
  private val cas2CancelledRuleSet: Cas2CancelledRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Short-term accommodation (ISR)",
    description = "Works out whether the person is eligible for CAS2 (ISR) accommodation and where they are in the process, from application through assessment, offer and arrival.",
    displayOrder = 2,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS2)
    val placeOffered = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_PLACE_OFFERED),
    )
    val awaitingDecision = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_AWAITING_DECISION),
    )
    val offerAccepted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_OFFER_ACCEPTED),
    )
    val onWaitingList = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_ON_WAITING_LIST),
    )
    val moreInformationNeeded = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_MORE_INFORMATION_NEEDED),
    )
    val awaitingArrival = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_AWAITING_ARRIVAL),
    )
    val arrived = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_ARRIVED),
    )
    val submitted = builder.outcome(
      ServiceResultV2(ServiceStatusV2.CAS2_SUBMITTED),
    )
    val leavingWithin15DaysRuleSetNode = builder
      .ruleSet(leavingWithin15DaysRuleSet)
      .onPass(confirmed)
      .onFail(confirmed).applyContextUpdaterOnFailure(cas2UpcomingContextUpdater)
      .build()

    val hasRoshLevelRuleSetNode = builder
      .ruleSet(hasRoshLevelRuleSet)
      .onPass(leavingWithin15DaysRuleSetNode)
      .onFail(notEligible)
      .build()

    val noNextAccommodationRuleSetNode = builder
      .ruleSet(noNextAccommodationRuleSet)
      .onPass(hasRoshLevelRuleSetNode)
      .onFail(notEligible)
      .build()

    val currentAccommodationNotCas2Cas3OrCommunityRuleSetNode = builder
      .ruleSet(currentAccommodationNotCas2Cas3OrCommunityRuleSet)
      .onPass(noNextAccommodationRuleSetNode)
      .onFail(notEligible)
      .build()

    val notInPrisonWithoutReleaseDateRuleSetNode = builder
      .ruleSet(notInPrisonWithoutReleaseDateRuleSet)
      .onPass(currentAccommodationNotCas2Cas3OrCommunityRuleSetNode)
      .onFail(notEligible)
      .build()

    val personEligibleRuleSetNode = builder
      .ruleSet(personEligibleRuleSet)
      .onPass(notInPrisonWithoutReleaseDateRuleSetNode)
      .onFail(notEligible)
      .build()

    val cas2MoreInformationRequestedRuleSetNode = builder
      .ruleSet(cas2MoreInformationRequestedRuleSet)
      .onPass(moreInformationNeeded)
      .onFail(awaitingDecision)
      .build()

    val cas2OnWaitingListRuleSetNode = builder
      .ruleSet(cas2OnWaitingListRuleSet)
      .onPass(onWaitingList)
      .onFail(cas2MoreInformationRequestedRuleSetNode)
      .build()

    val cas2PlaceOfferedRuleSetNode = builder
      .ruleSet(cas2PlaceOfferedRuleSet)
      .onPass(placeOffered)
      .onFail(cas2OnWaitingListRuleSetNode)
      .build()

    val cas2OfferAcceptedRuleSetNode = builder
      .ruleSet(cas2OfferAcceptedRuleSet)
      .onPass(offerAccepted)
      .onFail(cas2PlaceOfferedRuleSetNode)
      .build()

    val cas2AwaitingArrivalRuleSetNode = builder
      .ruleSet(cas2AwaitingArrivalRuleSet)
      .onPass(awaitingArrival)
      .onFail(cas2OfferAcceptedRuleSetNode)
      .build()

    val cas2MarkedAsArrivedRuleSetNode = builder
      .ruleSet(cas2MarkedAsArrivedRuleSet)
      .onPass(arrived)
      .onFail(cas2AwaitingArrivalRuleSetNode)
      .build()

    val cas2CancelledRuleSetNode = builder
      .ruleSet(cas2CancelledRuleSet)
      .onPass(personEligibleRuleSetNode).applyContextUpdaterOnPass(cas2CancelledContextUpdater)
      .onFail(personEligibleRuleSetNode).applyContextUpdaterOnFailure(cas2WithdrawnContextUpdater)
      .build()

    val cas2OfferDeclinedRuleSetNode = builder
      .ruleSet(cas2OfferDeclinedRuleSet)
      .onPass(personEligibleRuleSetNode).applyContextUpdaterOnPass(cas2OfferDeclinedContextUpdater)
      .onFail(cas2CancelledRuleSetNode)
      .build()

    val cas2ActiveRuleSetNode = builder
      .ruleSet(cas2ActiveRuleSet)
      .onPass(cas2MarkedAsArrivedRuleSetNode)
      .onFail(cas2OfferDeclinedRuleSetNode)
      .build()

    val cas2AssessedRuleSetNode = builder
      .ruleSet(cas2AssessedRuleSet)
      .onPass(cas2ActiveRuleSetNode)
      .onFail(submitted)
      .build()

    val cas2SubmittedRuleSetNode = builder
      .ruleSet(cas2SubmittedRuleSet)
      .onPass(cas2AssessedRuleSetNode)
      .onFail(personEligibleRuleSetNode).applyContextUpdaterOnFailure(cas2NotSubmittedContextUpdater)
      .build()

    return builder
      .ruleSet(cas2PresentRuleSet)
      .onPass(cas2SubmittedRuleSetNode)
      .onFail(personEligibleRuleSetNode).applyContextUpdaterOnFailure(cas2NotStartedContextUpdater)
      .build()
  }
}
