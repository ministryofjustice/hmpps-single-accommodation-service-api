package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.DtrActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.DtrRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.ReleaseWithin8WeeksRuleSet

@Component
class DtrEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin8Weeks: ReleaseWithin8WeeksRuleSet,
  private val upcomingContextUpdater: DtrUpcomingContextUpdater,
  private val referral: DtrRuleSet,
  private val activeContextUpdater: DtrActiveContextUpdater,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notRequired = builder.notRequired(ServiceType.DTR)
    val notStarted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.DTR_NOT_STARTED))

    // runs when person is eligible (no next accommodation) and has no active referral
    val releaseWithin8WeeksNode = builder
      .ruleSet(releaseWithin8Weeks)
      .applyContextUpdaterOnFailure(upcomingContextUpdater)
      .onPass(notStarted)
      .onFail(confirmed)
      .build()

    //  has next accommodation to produce NOT_REQUIRED
    val noNextAccommodationNode = builder
      .ruleSet(noNextAccommodation)
      .onPass(releaseWithin8WeeksNode)
      .onFail(notRequired)
      .build()

    // runs when there is an active referral
    // we do not care about the 8 week release window - surface the referral
    return builder
      .ruleSet(referral)
      .onPass(confirmed).applyContextUpdaterOnPass(activeContextUpdater)
      .onFail(noNextAccommodationNode)
      .build()
  }
}
