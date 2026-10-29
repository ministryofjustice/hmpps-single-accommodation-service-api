package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.CurrentAccommodationEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.TierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithin28WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshEligibleRuleSet

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin28Weeks: ReleaseWithin28WeeksRuleSet,
  private val upcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val referral: Cas1RuleSet,
  private val startContextUpdater: Cas1StartContextUpdater,
  private val activeContextUpdater: Cas1ActiveContextUpdater,
  private val tierEligibleForCas1: TierEligibleForCas1RuleSet,
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

    val tierEligibleForCas1Node = builder
      .ruleSet(tierEligibleForCas1)
      .onPass(currentAccommodationEligibleForCas1Node)
      .onFail(notEligible)
      .build()

    val referralNode = builder
      .ruleSet(referral)
      .onPass(confirmed).applyContextUpdaterOnPass(activeContextUpdater)
      .onFail(tierEligibleForCas1Node).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin28Weeks)
      .onPass(referralNode)
      .onFail(tierEligibleForCas1Node).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
