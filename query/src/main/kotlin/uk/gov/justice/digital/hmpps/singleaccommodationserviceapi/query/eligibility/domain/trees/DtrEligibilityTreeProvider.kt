package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.DtrActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin8WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrDomainData

@Component
class DtrEligibilityTreeProvider(
  engine: RulesEngine,
  private val releaseWithin8Weeks: ReleaseWithin8WeeksRuleSet,
  private val upcomingContextUpdater: DtrUpcomingContextUpdater,
  private val referral: DtrRuleSet,
  private val activeContextUpdater: DtrActiveContextUpdater,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
) : EligibilityTreeProvider<DtrDomainData> {
  private val builder = DecisionTreeBuilder<DtrDomainData>(engine)

  private val tree: DecisionNode<DtrDomainData> by lazy { build() }

  override fun tree(): DecisionNode<DtrDomainData> = tree

  override fun initialContext(data: DtrDomainData): EvaluationContext<DtrDomainData> = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode<DtrDomainData> {
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
