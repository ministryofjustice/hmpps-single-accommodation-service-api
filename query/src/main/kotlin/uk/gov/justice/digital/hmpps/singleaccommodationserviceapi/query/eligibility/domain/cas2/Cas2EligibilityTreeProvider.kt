package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityPersonRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityReleaseDateRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.EligibilityRoshRuleSet

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: Cas2UpcomingRuleSet,
  private val upcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val suitability: Cas2SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas2SuitabilityContextUpdater,
  private val completionContextUpdater: Cas2CompletionContextUpdater,
  private val eligibilityPerson: Cas2EligibilityPersonRuleSet,
  private val eligibilityReleaseDate: Cas2EligibilityReleaseDateRuleSet,
  private val eligibilityCurrentAccommodation: Cas2EligibilityCurrentAccommodationRuleSet,
  private val eligibilityNextAccommodation: Cas2EligibilityNextAccommodationRuleSet,
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
    val notEligible = builder.notEligible(ServiceType.CAS2)

    val eligibilityRoshNode = builder
      .ruleSet(eligibilityRosh)
      .onPass(confirmed)
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

    val eligibilityReleaseDateNode = builder
      .ruleSet(eligibilityReleaseDate)
      .onPass(eligibilityCurrentAccommodationNode)
      .onFail(notEligible)
      .build()

    val eligibilityPersonNode = builder
      .ruleSet(eligibilityPerson)
      .onPass(eligibilityReleaseDateNode)
      .onFail(notEligible)
      .build()

    val suitabilityNode = builder
      .ruleSet(suitability)
      .onPass(confirmed).applyContextUpdaterOnPass(completionContextUpdater)
      .onFail(eligibilityPersonNode).applyContextUpdaterOnFailure(suitabilityContextUpdater)
      .build()

    return builder
      .ruleSet(upcoming)
      .onPass(suitabilityNode)
      .onFail(eligibilityPersonNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
