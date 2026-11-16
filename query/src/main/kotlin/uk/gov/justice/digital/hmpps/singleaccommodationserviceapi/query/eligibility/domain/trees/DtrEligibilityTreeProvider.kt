package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrNotAcceptedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.DtrRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets.ReleaseWithin8WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class DtrEligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin8Weeks: ReleaseWithin8WeeksRuleSet,
  private val upcomingContextUpdater: DtrUpcomingContextUpdater,
  private val referral: DtrRuleSet,
  private val acceptedRuleSet: DtrAcceptedRuleSet,
  private val notAcceptedRuleSet: DtrNotAcceptedRuleSet,
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
    val submitted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.DTR_SUBMITTED))
    val accepted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.DTR_ACCEPTED))
    val notAccepted = builder.outcome(ServiceResultNew(serviceStatus = ServiceStatusNew.DTR_NOT_ACCEPTED))

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

    val notAcceptedNode = builder
      .ruleSet(notAcceptedRuleSet)
      .onPass(notAccepted)
      .onFail(submitted)
      .build()

    val acceptedNode = builder
      .ruleSet(acceptedRuleSet)
      .onPass(accepted)
      .onFail(notAcceptedNode)
      .build()

    // runs when there is an active referral
    // we do not care about the 8 week release window - surface the referral
    return builder
      .ruleSet(referral)
      .onPass(acceptedNode)
      .onFail(noNextAccommodationNode)
      .build()
  }
}
