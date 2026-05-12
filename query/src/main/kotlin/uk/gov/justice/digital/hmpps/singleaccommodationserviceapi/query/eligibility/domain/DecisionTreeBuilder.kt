package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

/**
 * Generic builder for constructing decision trees. Allows declaratively to chain rulesets and outcomes.
 */
@Component
class DecisionTreeBuilder(
  @param:Qualifier("defaultRulesEngine")
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

  /**
   * On FAIL, replace the current ServiceResult with [onFailResult].
   * Use when the FAIL outcome is a static refinement that does not depend on the existing context.
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
