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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligible.NoActiveCas1OrCas3ReferralsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.HasNextAccommodationRuleSet

@Component
class PaEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val noActiveCas1OrCas3Referrals: NoActiveCas1OrCas3ReferralsRuleSet,
  private val hasNextAccommodation: HasNextAccommodationRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val notEligible = builder.notEligible(ServiceType.PA)
    val completed = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_COMPLETED))
    val notStarted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_NOT_STARTED))

    val noActiveCas1OrCas3ReferralsNode = builder
      .ruleSet(noActiveCas1OrCas3Referrals)
      .onPass(notStarted)
      .onFail(notEligible)
      .build()

    return builder
      .ruleSet(hasNextAccommodation)
      .onPass(completed)
      .onFail(noActiveCas1OrCas3ReferralsNode)
      .build()
  }
}
