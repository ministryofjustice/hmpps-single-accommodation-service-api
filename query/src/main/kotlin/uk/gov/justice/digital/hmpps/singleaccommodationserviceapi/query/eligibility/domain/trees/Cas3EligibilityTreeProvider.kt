package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3CannotStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotStartedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotStartedNewContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3RejectedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3ActiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3BedspaceOfferedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3BookingCancelledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3BookingConfirmedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3MarkedAsArrivedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3MarkedAsNotArrivedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3PresentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3RejectedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas3SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationEligibleForCas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class Cas3EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin15DaysRuleSet: ReleaseWithin15DaysRuleSet,
  private val upcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val presentRuleSet: Cas3PresentRuleSet,
  private val submittedRuleSet: Cas3SubmittedRuleSet,
  private val rejectedRuleSet: Cas3RejectedRuleSet,
  private val activeRuleSet: Cas3ActiveRuleSet,
  private val notStartedContextUpdater: Cas3NotStartedContextUpdater,
  private val notSubmittedContextUpdater: Cas3NotSubmittedContextUpdater,
  private val rejectedContextUpdater: Cas3RejectedContextUpdater,
  private val notStartedNewContextUpdater: Cas3NotStartedNewContextUpdater,
  private val bookingConfirmedRuleSet: Cas3BookingConfirmedRuleSet,
  private val markedAsArrivedRuleSet: Cas3MarkedAsArrivedRuleSet,
  private val bedspaceOfferedRuleSet: Cas3BedspaceOfferedRuleSet,
  private val markedAsNotArrivedRuleSet: Cas3MarkedAsNotArrivedRuleSet,
  private val bookingCancelledRuleSet: Cas3BookingCancelledRuleSet,
  private val currentAccommodationEligible: CurrentAccommodationEligibleForCas3RuleSet,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
  private val prerequisite: Cas3PrerequisiteRuleSet,
  private val cannotStartContextUpdater: Cas3CannotStartContextUpdater,
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
    val notEligible = builder.notEligible(ServiceType.CAS3)
    val bookingConfirmed = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_BOOKING_CONFIRMED),
    )
    val arrived = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_ARRIVED),
    )
    val bedspaceOffered = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_BEDSPACE_OFFERED),
    )
    val notArrived = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_NOT_ARRIVED),
    )
    val bookingCancelled = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_BOOKING_CANCELLED),
    )
    val submitted = builder.outcome(
      ServiceResultNew(ServiceStatusNew.CAS3_SUBMITTED),
    )

    val prerequisiteNode = builder
      .ruleSet(prerequisite)
      .onPass(confirmed)
      .onFail(confirmed).applyContextUpdaterOnFailure(cannotStartContextUpdater)
      .build()

    val roshEligibleNode = builder
      .ruleSet(roshEligible)
      .onPass(prerequisiteNode)
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

    val bookingCancelledRuleSetNode = builder
      .ruleSet(bookingCancelledRuleSet)
      .onPass(bookingCancelled)
      .onFail(submitted)
      .build()

    val markedAsNotArrivedRuleSetNode = builder
      .ruleSet(markedAsNotArrivedRuleSet)
      .onPass(notArrived)
      .onFail(bookingCancelledRuleSetNode)
      .build()

    val bedspaceOfferedRuleSetNode = builder
      .ruleSet(bedspaceOfferedRuleSet)
      .onPass(bedspaceOffered)
      .onFail(markedAsNotArrivedRuleSetNode)
      .build()

    val markedAsArrivedRuleSetNode = builder
      .ruleSet(markedAsArrivedRuleSet)
      .onPass(arrived)
      .onFail(bedspaceOfferedRuleSetNode)
      .build()

    val bookingConfirmedRuleSetNode = builder
      .ruleSet(bookingConfirmedRuleSet)
      .onPass(bookingConfirmed)
      .onFail(markedAsArrivedRuleSetNode)
      .build()

    val activeRuleSetNode = builder
      .ruleSet(activeRuleSet)
      .onPass(bookingConfirmedRuleSetNode)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(notStartedNewContextUpdater)
      .build()

    val rejectedRuleSetNode = builder
      .ruleSet(rejectedRuleSet)
      .onPass(currentAccommodationEligibleNode).applyContextUpdaterOnPass(rejectedContextUpdater)
      .onFail(activeRuleSetNode)
      .build()

    val submittedRuleSetNode = builder
      .ruleSet(submittedRuleSet)
      .onPass(rejectedRuleSetNode)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(notSubmittedContextUpdater)
      .build()

    val presentRuleSetNode = builder
      .ruleSet(presentRuleSet)
      .onPass(submittedRuleSetNode)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(notStartedContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin15DaysRuleSet)
      .onPass(presentRuleSetNode)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
