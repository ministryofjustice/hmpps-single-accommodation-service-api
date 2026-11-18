package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PpcAnswersPresentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PpcPublicProtectionNotRequiredRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.PpcRequiredRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class PpcEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val ppcAnswersPresentRuleSet: PpcAnswersPresentRuleSet,
  private val ppcPublicProtectionNotRequiredRuleSet: PpcPublicProtectionNotRequiredRuleSet,
  private val ppcRequiredRuleSet: PpcRequiredRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Public Protection Checker",
    description = "Reports whether the Public Protection Checker has been completed and whether a protection need was identified.",
    displayOrder = 7,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val notRequired = builder.notRequired(ServiceType.PPC)
    val notStarted = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.PPC_NOT_STARTED))
    val completedPpNotRequired = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.PPC_COMPLETED_PUBLIC_PROTECTION_NOT_REQUIRED))
    val completedPpRequired = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.PPC_COMPLETED_PUBLIC_PROTECTION_REQUIRED))

    val ppcRequiredRuleSetNode = builder
      .ruleSet(ppcRequiredRuleSet)
      .onPass(notStarted)
      .onFail(notRequired)
      .build()

    val ppcPublicProtectionNotRequiredRuleSetNode = builder
      .ruleSet(ppcPublicProtectionNotRequiredRuleSet)
      .onPass(completedPpNotRequired)
      .onFail(completedPpRequired)
      .build()

    return builder
      .ruleSet(ppcAnswersPresentRuleSet)
      .onPass(ppcPublicProtectionNotRequiredRuleSetNode)
      .onFail(ppcRequiredRuleSetNode)
      .build()
  }
}
