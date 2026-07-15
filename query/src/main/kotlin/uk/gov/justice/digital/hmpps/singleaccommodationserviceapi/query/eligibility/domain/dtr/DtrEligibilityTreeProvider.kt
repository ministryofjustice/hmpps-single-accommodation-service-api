package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingRuleSet

@Component
class DtrEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val upcoming: DtrUpcomingRuleSet,
  private val upcomingContextUpdater: DtrUpcomingContextUpdater,
  private val suitability: DtrSuitabilityRuleSet,
  private val completion: DtrCompletionRuleSet,
  private val completionContextUpdater: DtrCompletionContextUpdater,
  private val eligibility: DtrEligibilityRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = ServiceResult(serviceStatus = ServiceStatus.ACCEPTED),
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notRequired = builder.notRequired()
    val accepted = builder.outcome(ServiceResult(ServiceStatus.ACCEPTED))

    val eligibilityNode = builder
      .ruleSet("DtrEligibility", eligibility)
      .onPass(confirmed)
      .onFail(notRequired)
      .build()

    val completionNode = builder
      .ruleSet("DtrCompletion", completion, completionContextUpdater)
      .onPass(accepted)
      .onFail(confirmed)
      .build()

    // runs when there is no active referral found
    // checks if release within 8 weeks to prompt
    val upcomingNode = builder
      .ruleSet("DtrUpcoming", upcoming, upcomingContextUpdater)
      .onPass(eligibilityNode)
      .onFail(eligibilityNode)
      .build()

    // runs when there is an active referral
    // we dont care about the 8 week release window - surface the referral
    return builder
      .ruleSet(
        "DtrSuitability",
        suitability,
        onFailResult = ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = CaseAction(type = CaseActionType.ADD_DTR_REFERRAL_DETAILS),
          link = EligibilityKeys.ADD_REFERRAL_DETAILS,
        ),
      )
      .onPass(completionNode)
      .onFail(upcomingNode)
      .build()
  }
}
