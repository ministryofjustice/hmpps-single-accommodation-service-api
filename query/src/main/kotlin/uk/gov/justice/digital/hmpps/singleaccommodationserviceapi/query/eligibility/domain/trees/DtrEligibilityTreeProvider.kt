package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrNotAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrPresentNotWithdrawnOrExpiredRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LeavingWithin8WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class DtrEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val leavingWithin8WeeksRuleSet: LeavingWithin8WeeksRuleSet,
  private val dtrUpcomingContextUpdater: DtrUpcomingContextUpdater,
  private val dtrPresentNotWithdrawnOrExpiredRuleSet: DtrPresentNotWithdrawnOrExpiredRuleSet,
  private val dtrAcceptedRuleSet: DtrAcceptedRuleSet,
  private val dtrNotAcceptedRuleSet: DtrNotAcceptedRuleSet,
  private val noNextAccommodationRuleSet: NoNextAccommodationRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Duty to Refer",
    description = "Works out whether a Duty to Refer to the local authority is needed and tracks whether it has been submitted, accepted or not accepted.",
    displayOrder = 4,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notRequired = builder.notRequired(ServiceType.DTR)
    val notStarted = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.DTR_NOT_STARTED))
    val submitted = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.DTR_SUBMITTED))
    val accepted = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.DTR_ACCEPTED))
    val notAccepted = builder.outcome(ServiceResultV2(serviceStatus = ServiceStatusV2.DTR_NOT_ACCEPTED))

    // runs when person is eligible (no next accommodation) and has no active dtrPresentNotWithdrawnOrExpiredRuleSet
    val leavingWithin8WeeksRuleSetNode = builder
      .ruleSet(leavingWithin8WeeksRuleSet)
      .applyContextUpdaterOnFailure(dtrUpcomingContextUpdater)
      .onPass(notStarted)
      .onFail(confirmed)
      .build()

    //  has next accommodation to produce NOT_REQUIRED
    val noNextAccommodationRuleSetNode = builder
      .ruleSet(noNextAccommodationRuleSet)
      .onPass(leavingWithin8WeeksRuleSetNode)
      .onFail(notRequired)
      .build()

    val dtrNotAcceptedRuleSetNode = builder
      .ruleSet(dtrNotAcceptedRuleSet)
      .onPass(notAccepted)
      .onFail(submitted)
      .build()

    val dtrAcceptedRuleSetNode = builder
      .ruleSet(dtrAcceptedRuleSet)
      .onPass(accepted)
      .onFail(dtrNotAcceptedRuleSetNode)
      .build()

    // runs when there is an active dtrPresentNotWithdrawnOrExpiredRuleSet
    // we do not care about the 8 week release window - surface the dtrPresentNotWithdrawnOrExpiredRuleSet
    return builder
      .ruleSet(dtrPresentNotWithdrawnOrExpiredRuleSet)
      .onPass(dtrAcceptedRuleSetNode)
      .onFail(noNextAccommodationRuleSetNode)
      .build()
  }
}
