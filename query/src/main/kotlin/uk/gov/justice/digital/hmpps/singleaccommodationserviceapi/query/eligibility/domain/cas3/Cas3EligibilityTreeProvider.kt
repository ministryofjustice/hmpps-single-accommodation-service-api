package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligible.CurrentAccommodationEligibleForCas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3CannotStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshEligibleRuleSet

@Component
class Cas3EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin15DaysRuleSet: ReleaseWithin15DaysRuleSet,
  private val upcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val referral: Cas3RuleSet,
  private val startContextUpdater: Cas3StartContextUpdater,
  private val activeContextUpdater: Cas3ActiveContextUpdater,
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

    val referralNode = builder
      .ruleSet(referral)
      .onPass(confirmed).applyContextUpdaterOnPass(activeContextUpdater)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin15DaysRuleSet)
      .onPass(referralNode)
      .onFail(currentAccommodationEligibleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
