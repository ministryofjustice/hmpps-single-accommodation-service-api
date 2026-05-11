package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/**
 * Generic builder for constructing decision trees. Allows declaratively to chain rulesets and outcomes.
 */
class DecisionTreeBuilder(
  private val engine: RulesEngine,
) {
  /**
   * Starts building a RuleSetNode with the given ruleset and context updater. Returns a RuleSetNodeBuilder
   */
  fun ruleSet(
    name: String,
    ruleSet: RuleSet,
    contextUpdater: ContextUpdater,
  ) = RuleSetNodeBuilder(name, ruleSet, contextUpdater, engine)

  /** Creates a terminal outcome node that returns a fixed ServiceResult. */
  fun outcome(result: ServiceResult) = OutcomeNode { _ -> result }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed() = OutcomeNode { ctx ->
    val result = ctx.currentResult
    if (result.serviceStatus == ServiceStatus.NOT_ELIGIBLE) {
      result
    } else {
      result.copy(failureReasons = emptyList())
    }
  }

  /** Creates a terminal outcome node for NOT_ELIGIBLE status */
  fun notEligible() = OutcomeNode { ctx -> ServiceResult(ServiceStatus.NOT_ELIGIBLE, failureReasons = ctx.currentResult.failureReasons) }

  fun accepted() = outcome(ServiceResult(ServiceStatus.ACCEPTED))

  fun bookingConfirmed() = outcome(
    ServiceResult(
      serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
      link = EligibilityKeys.VIEW_REFERRAL,
    ),
  )

  fun placementBooked() = outcome(
    ServiceResult(
      serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
      link = EligibilityKeys.VIEW_APPLICATION,
    ),
  )
}
