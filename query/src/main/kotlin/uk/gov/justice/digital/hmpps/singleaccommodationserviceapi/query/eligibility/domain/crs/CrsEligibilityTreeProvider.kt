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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilitySettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingRuleSet

@Component
class CrsEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val eligibilitySettled: CrsEligibilitySettledRuleSet,
  private val eligibilityNextAccommodation: CrsEligibilityNextAccommodationRuleSet,
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
    val submitted = builder.outcome(
      ServiceResultNew(serviceStatus = ServiceStatusNew.CRS_SUBMITTED, url = url),
    )

    val eligibilityNextAccommodationNode = builder
      .ruleSet(eligibilityNextAccommodation)
      .onPass(confirmed)
      .onFail(builder.notRequired(ServiceType.CRS, eligibilityNextAccommodation.failureReasons())).propagateContextOnFailure()
      .build()

    val eligibilitySettledNode = builder
      .ruleSet(eligibilitySettled)
      .onPass(eligibilityNextAccommodationNode)
      .onFail(builder.notRequired(ServiceType.CRS, eligibilitySettled.failureReasons())).propagateContextOnFailure()
      .build()

    val completionNode = builder
      .ruleSet(completion)
      .onPass(submitted)
      .onFail(eligibilitySettledNode).applyContextUpdaterOnFailure(completionContextUpdater)
      .build()

    return builder
      .ruleSet(upcoming)
      .onPass(completionNode)
      .onFail(eligibilitySettledNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
