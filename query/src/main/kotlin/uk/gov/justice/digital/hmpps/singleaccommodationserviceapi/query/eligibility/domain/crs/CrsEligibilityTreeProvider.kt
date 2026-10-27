package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRuleSet

@Component
class CrsEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val eligibility: CrsEligibilityRuleSet,
  private val completion: CrsCompletionRuleSet,
  private val completionContextUpdater: CrsCompletionContextUpdater,
  private val upcoming: CrsUpcomingRuleSet,
  private val upcomingContextUpdater: CrsUpcomingContextUpdater,
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : EligibilityTreeProvider {

  val url = crsUiBaseUrl

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notRequired = builder.notRequired(ServiceType.CRS)
    val submitted = builder.outcome(
      "submitted",
      ServiceResultNew(serviceStatus = ServiceStatusNew.CRS_SUBMITTED, url = url),
    )

    val eligibilityNode = builder
      .ruleSet("CrsEligibility", eligibility)
      .onPass(confirmed)
      .onFail(notRequired).propagateContextOnFailure()
      .build()

    val completionNode = builder
      .ruleSet("CrsCompletion", completion)
      .onPass(submitted)
      .onFail(eligibilityNode).applyContextUpdaterOnFailure(completionContextUpdater)
      .build()

    return builder
      .ruleSet("CrsUpcoming", upcoming)
      .onPass(completionNode)
      .onFail(eligibilityNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
