package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityTierRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.EligibilityRoshRuleSet

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: Cas1UpcomingRuleSet,
  private val upcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val suitability: Cas1SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas1SuitabilityContextUpdater,
  private val completionContextUpdater: Cas1CompletionContextUpdater,
  private val eligibilityTier: Cas1EligibilityTierRuleSet,
  private val eligibilityCurrentAccommodation: Cas1EligibilityCurrentAccommodationRuleSet,
  private val eligibilityRosh: EligibilityRoshRuleSet,
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

    val eligibilityRoshNode = builder
      .ruleSet(eligibilityRosh)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val eligibilityCurrentAccommodationNode = builder
      .ruleSet(eligibilityCurrentAccommodation)
      .onPass(eligibilityRoshNode)
      .onFail(notEligible)
      .build()

    val eligibilityTierNode = builder
      .ruleSet(eligibilityTier)
      .onPass(eligibilityCurrentAccommodationNode)
      .onFail(notEligible)
      .build()

    val suitabilityNode = builder
      .ruleSet(suitability)
      .onPass(confirmed).applyContextUpdaterOnPass(completionContextUpdater)
      .onFail(eligibilityTierNode).applyContextUpdaterOnFailure(suitabilityContextUpdater)
      .build()

    return builder
      .ruleSet(upcoming)
      .onPass(suitabilityNode)
      .onFail(eligibilityTierNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
