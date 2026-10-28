package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
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
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val completed = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_COMPLETED))

    val eligibilityNode = builder
      .ruleSet(eligibility)
      .onPass(confirmed)
      .onFail(builder.notEligible(ServiceType.PA, eligibility.failureReasons())).propagateContextOnFailure()
      .build()

    return builder
      .ruleSet(completion)
      .onPass(completed)
      .onFail(eligibilityNode).setConstantServiceResultOnFailure(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_NOT_STARTED))
      .build()
  }
}
