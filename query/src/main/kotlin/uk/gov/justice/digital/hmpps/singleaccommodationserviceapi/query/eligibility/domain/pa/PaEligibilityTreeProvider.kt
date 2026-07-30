package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.PaEligibilityRuleSet

@Component
class PaEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val eligibility: PaEligibilityRuleSet,
  private val completion: PaCompletionRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = ServiceResult(serviceStatus = ServiceStatus.COMPLETED),
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible()

    val eligibilityNode = builder
      .ruleSet("PaEligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    return builder
      .ruleSet(
        "PaCompletion",
        completion,
        onFailResult = ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = CaseAction(type = CaseActionType.ADD_AND_CONFIRM_PROPOSED_ADDRESS),
        ),
      )
      .onPass(confirmed)
      .onFail(eligibilityNode)
      .build()
  }
}
