package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.EligibilityRoshRuleSet

@Component
class Cas3EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: Cas3UpcomingRuleSet,
  private val upcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val suitability: Cas3SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas3SuitabilityContextUpdater,
  private val completionContextUpdater: Cas3CompletionContextUpdater,
  private val eligibilityCurrentAccommodation: Cas3EligibilityCurrentAccommodationRuleSet,
  private val eligibilityNextAccommodation: Cas3EligibilityNextAccommodationRuleSet,
  private val prerequisite: Cas3PrerequisiteRuleSet,
  private val cas3PrerequisiteContextUpdater: Cas3PrerequisiteContextUpdater,
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
    val notEligible = builder.notEligible(ServiceType.CAS3)

    val prerequisiteNode = builder
      .ruleSet(prerequisite)
      .onPass(confirmed)
      .onFail(confirmed).applyContextUpdaterOnFailure(cas3PrerequisiteContextUpdater)
      .build()

    val eligibilityRoshNode = builder
      .ruleSet(eligibilityRosh)
      .onPass(prerequisiteNode)
      .onFail(notEligible)
      .build()

    val eligibilityNextAccommodationNode = builder
      .ruleSet(eligibilityNextAccommodation)
      .onPass(eligibilityRoshNode)
      .onFail(notEligible)
      .build()

    val eligibilityCurrentAccommodationNode = builder
      .ruleSet(eligibilityCurrentAccommodation)
      .onPass(eligibilityNextAccommodationNode)
      .onFail(notEligible)
      .build()

    val suitabilityNode = builder
      .ruleSet(suitability)
      .onPass(confirmed).applyContextUpdaterOnPass(completionContextUpdater)
      .onFail(eligibilityCurrentAccommodationNode).applyContextUpdaterOnFailure(suitabilityContextUpdater)
      .build()

    return builder
      .ruleSet(upcoming)
      .onPass(suitabilityNode)
      .onFail(eligibilityCurrentAccommodationNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
