package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2ActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.Cas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseDateEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.RoshEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

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
