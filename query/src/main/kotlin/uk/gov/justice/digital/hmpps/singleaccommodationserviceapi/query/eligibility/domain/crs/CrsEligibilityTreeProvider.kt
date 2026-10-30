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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligible.NotSettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.CrsStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.LiveCrsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.ReleaseWithin12WeeksRuleSet

@Component
class CrsEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val notSettledRuleSet: NotSettledRuleSet,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
  private val liveCrs: LiveCrsRuleSet,
  private val startContextUpdater: CrsStartContextUpdater,
  private val upcoming: ReleaseWithin12WeeksRuleSet,
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
      ServiceResultNew(serviceStatus = ServiceStatusNew.CRS_SUBMITTED, url = url),
    )

    val noNextAccommodationNode = builder
      .ruleSet(noNextAccommodation)
      .onPass(confirmed)
      .onFail(notRequired)
      .build()

    val notSettledNode = builder
      .ruleSet(notSettledRuleSet)
      .onPass(noNextAccommodationNode)
      .onFail(notRequired)
      .build()

    val liveCrsNode = builder
      .ruleSet(liveCrs)
      .onPass(submitted)
      .onFail(notSettledNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(upcoming)
      .onPass(liveCrsNode)
      .onFail(notSettledNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
