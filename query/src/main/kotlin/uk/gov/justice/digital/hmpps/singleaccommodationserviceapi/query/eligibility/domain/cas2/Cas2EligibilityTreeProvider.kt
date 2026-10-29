package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.ReleaseDateEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshEligibleRuleSet

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin15Days: ReleaseWithin15DaysRuleSet,
  private val upcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val referral: Cas2RuleSet,
  private val startContextUpdater: Cas2StartContextUpdater,
  private val activeContextUpdater: Cas2ActiveContextUpdater,
  private val personEligible: PersonEligibleRuleSet,
  private val releaseDateEligible: ReleaseDateEligibleRuleSet,
  private val currentAccommodationEligible: CurrentAccommodationEligibleForCas2RuleSet,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
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
    val notEligible = builder.notEligible(ServiceType.CAS2)

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

    val referralNode = builder
      .ruleSet(referral)
      .onPass(confirmed).applyContextUpdaterOnPass(activeContextUpdater)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin15Days)
      .onPass(referralNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
