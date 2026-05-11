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
   * Starts building a RuleSetNode with the given ruleset and context updater. Returns a RuleSetNodeBuilder.
   */
  fun ruleSet(
    name: String,
    ruleSet: RuleSet,
    contextUpdater: ContextUpdater,
  ) = RuleSetNodeBuilder(name, ruleSet, contextUpdater, engine)

  /**
   * On FAIL, replace the current ServiceResult with [onFailResult].
   * Use when the FAIL outcome does not depend on the existing context.
   */
  fun ruleSet(
    name: String,
    ruleSet: RuleSet,
    onFailResult: ServiceResult,
  ) = ruleSet(name, ruleSet, ContextUpdater.constant(onFailResult))

  /**
   * On FAIL, leave the current ServiceResult unchanged.
   * Use when the FAIL branch ends in a terminal outcome that ignores the context anyway.
   */
  fun ruleSet(
    name: String,
    ruleSet: RuleSet,
  ) = ruleSet(name, ruleSet, ContextUpdater.identity())

  /** Creates a terminal outcome node that returns a fixed ServiceResult. */
  fun outcome(result: ServiceResult) = OutcomeNode { _ -> result }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed() = OutcomeNode { context -> context.currentResult }

  /** Creates a terminal outcome node for NOT_ELIGIBLE status */
  fun notEligible() = outcome(ServiceResult(ServiceStatus.NOT_ELIGIBLE))

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
