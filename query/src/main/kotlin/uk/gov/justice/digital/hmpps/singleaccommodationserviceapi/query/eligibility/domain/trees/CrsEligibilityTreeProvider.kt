package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.CrsStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.CrsLiveRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LeavingWithin12WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotSettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeInfo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class CrsEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val notSettledRuleSet: NotSettledRuleSet,
  private val noNextAccommodationRuleSet: NoNextAccommodationRuleSet,
  private val crsLiveRuleSet: CrsLiveRuleSet,
  private val crsStartContextUpdater: CrsStartContextUpdater,
  private val leavingWithin12WeeksRuleSet: LeavingWithin12WeeksRuleSet,
  private val crsUpcomingContextUpdater: CrsUpcomingContextUpdater,
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : EligibilityTreeProvider {

  val url = crsUiBaseUrl

  private val tree: DecisionNode by lazy { build() }

  override val treeInfo = EligibilityTreeInfo(
    fullName = "Commissioned Rehabilitative Services",
    description = "Works out whether a Commissioned Rehabilitative Services (CRS) accommodation referral is needed and whether a live referral exists.",
    displayOrder = 5,
  )

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notRequired = builder.notRequired(ServiceType.CRS)
    val submitted = builder.outcome(
      ServiceResultV2(serviceStatus = ServiceStatusV2.CRS_SUBMITTED, url = url),
    )

    val noNextAccommodationRuleSetNode = builder
      .ruleSet(noNextAccommodationRuleSet)
      .onPass(confirmed)
      .onFail(notRequired)
      .build()

    val notSettledRuleSetNode = builder
      .ruleSet(notSettledRuleSet)
      .onPass(noNextAccommodationRuleSetNode)
      .onFail(notRequired)
      .build()

    val crsLiveRuleSetNode = builder
      .ruleSet(crsLiveRuleSet)
      .onPass(submitted)
      .onFail(notSettledRuleSetNode).applyContextUpdaterOnFailure(crsStartContextUpdater)
      .build()

    return builder
      .ruleSet(leavingWithin12WeeksRuleSet)
      .onPass(crsLiveRuleSetNode)
      .onFail(notSettledRuleSetNode).applyContextUpdaterOnFailure(crsUpcomingContextUpdater)
      .build()
  }
}
