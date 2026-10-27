package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: Cas1UpcomingRuleSet,
  private val upcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val suitability: Cas1SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas1SuitabilityContextUpdater,
  private val completionContextUpdater: Cas1CompletionContextUpdater,
  private val eligibility: Cas1EligibilityRuleSet,
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

    val eligibilityNode = builder
      .ruleSet("Cas1Eligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible).propagateContextOnFailure()
      .build()

    val suitabilityNode = builder
      .ruleSet("Cas1Suitability", suitability)
      .onPass(confirmed).applyContextUpdaterOnPass(completionContextUpdater)
      .onFail(eligibilityNode).applyContextUpdaterOnFailure(suitabilityContextUpdater)
      .build()

    return builder
      .ruleSet("Cas1Upcoming", upcoming)
      .onPass(suitabilityNode)
      .onFail(eligibilityNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
