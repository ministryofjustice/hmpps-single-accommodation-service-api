package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.CrsStartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.LiveCrsRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NotSettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin12WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

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
