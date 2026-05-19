package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet

@Component
class CrsEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val eligibility: CrsEligibilityRuleSet,
  private val completion: CrsCompletionRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
    ),
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible()

    val eligibilityNode = builder
      .ruleSet("CrsEligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    return builder
      .ruleSet(
        "CrsCompletion",
        completion,
        onFailResult = ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = EligibilityKeys.SUBMIT_CRS_REFERRAL,
          link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
        ),
      )
      .onPass(confirmed)
      .onFail(eligibilityNode)
      .build()
  }
}
