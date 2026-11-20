package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.HasNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoActiveCas1OrCas3ApplicationsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class PaEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val noActiveCas1OrCas3ApplicationsRuleSet: NoActiveCas1OrCas3ApplicationsRuleSet,
  private val hasNextAccommodationRuleSet: HasNextAccommodationRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Proposed Accommodation",
    description = "Works out whether the person has proposed (next) accommodation and whether any CAS1 or CAS3 applications are still active.",
    displayOrder = 6,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val notEligible = builder.notEligible(ServiceType.PA)
    val completed = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_COMPLETED))
    val notStarted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.PA_NOT_STARTED))

    val noActiveCas1OrCas3ApplicationsRuleSetNode = builder
      .ruleSet(noActiveCas1OrCas3ApplicationsRuleSet)
      .onPass(notStarted)
      .onFail(notEligible)
      .build()

    return builder
      .ruleSet(hasNextAccommodationRuleSet)
      .onPass(completed)
      .onFail(noActiveCas1OrCas3ApplicationsRuleSetNode)
      .build()
  }
}
