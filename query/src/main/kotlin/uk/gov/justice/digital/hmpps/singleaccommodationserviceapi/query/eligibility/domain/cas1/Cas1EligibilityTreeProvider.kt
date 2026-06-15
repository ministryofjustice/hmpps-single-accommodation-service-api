package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet

@Component
class Cas1EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val validation: Cas1ValidationRuleSet,
  private val upcoming: Cas1UpcomingRuleSet,
  private val upcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val suitability: Cas1SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas1SuitabilityContextUpdater,
  private val completion: Cas1CompletionRuleSet,
  private val completionContextUpdater: Cas1CompletionContextUpdater,
  private val eligibility: Cas1EligibilityRuleSet,
  private val deeplinkResolver: Cas1DeeplinkResolver,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = serviceResult(),
  )

  override fun resolveDeeplink(result: ServiceResult, data: DomainData): ServiceResult = deeplinkResolver.resolve(result, data)

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible()
    val placementBooked = builder.outcome(
      serviceResult(),
    )

    val eligibilityNode = builder
      .ruleSet("Cas1Eligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val completionNode = builder
      .ruleSet("Cas1Completion", completion, completionContextUpdater)
      .onPass(placementBooked)
      .onFail(confirmed)
      .build()

    val suitabilityNode = builder
      .ruleSet("Cas1Suitability", suitability, suitabilityContextUpdater)
      .onPass(completionNode)
      .onFail(eligibilityNode)
      .build()

    val upcomingNode = builder
      .ruleSet("Cas1Upcoming", upcoming, upcomingContextUpdater)
      .onPass(suitabilityNode)
      .onFail(eligibilityNode)
      .build()

    return builder
      .ruleSet("Cas1Validation", validation)
      .onPass(upcomingNode)
      .onFail(notEligible)
      .build()
  }

  private fun serviceResult(): ServiceResult = ServiceResult(
    serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
    link = EligibilityKeys.VIEW_APPLICATION,
    linkType = LinkType.CAS1_VIEW_APPLICATION,
  )
}
