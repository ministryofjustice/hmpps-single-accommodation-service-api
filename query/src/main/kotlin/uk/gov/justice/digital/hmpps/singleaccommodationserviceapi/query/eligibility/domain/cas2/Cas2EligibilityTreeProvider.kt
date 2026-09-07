package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingRuleSet

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: Cas2UpcomingRuleSet,
  private val upcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val suitability: Cas2SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas2SuitabilityContextUpdater,
  private val completion: Cas2CompletionRuleSet,
  private val completionContextUpdater: Cas2CompletionContextUpdater,
  private val eligibility: Cas2EligibilityRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = serviceResult(),
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible()
    val placementBooked = builder.outcome(
      serviceResult(),
    )

    val eligibilityNode = builder
      .ruleSet("Cas2Eligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val completionNode = builder
      .ruleSet("Cas2Completion", completion, completionContextUpdater)
      .onPass(placementBooked)
      .onFail(confirmed)
      .build()

    val suitabilityNode = builder
      .ruleSet("Cas2Suitability", suitability, suitabilityContextUpdater)
      .onPass(completionNode)
      .onFail(eligibilityNode)
      .build()

    return builder
      .ruleSet("Cas2Upcoming", upcoming, upcomingContextUpdater)
      .onPass(suitabilityNode)
      .onFail(eligibilityNode)
      .build()
  }

  private fun serviceResult(): ServiceResult = ServiceResult(
    serviceStatus = ServiceStatus.COMPLETED,
    link = EligibilityKeys.VIEW_APPLICATION,
    linkType = LinkType.CAS2_VIEW_APPLICATION,
  )
}
