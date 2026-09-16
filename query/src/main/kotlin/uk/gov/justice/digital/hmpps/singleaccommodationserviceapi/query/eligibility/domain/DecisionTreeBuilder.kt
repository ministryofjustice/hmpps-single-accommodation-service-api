package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotRequiredServiceStatus
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
    onFailResult: ServiceResultNew,
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
  fun outcome(name: String, result: ServiceResultNew) = OutcomeNode(name) { _ -> result }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed() = OutcomeNode("confirmed") { ctx ->
    val result = ctx.currentResult
    when (result.serviceStatus) {
      ServiceStatusNew.CAS1_NOT_ELIGIBLE,
      ServiceStatusNew.CAS2_NOT_ELIGIBLE,
      ServiceStatusNew.CAS3_NOT_ELIGIBLE,
      ServiceStatusNew.CRS_NOT_REQUIRED,
      ServiceStatusNew.DTR_NOT_REQUIRED,
      ServiceStatusNew.CRS_NOT_ELIGIBLE,
      ServiceStatusNew.DTR_NOT_ELIGIBLE,
      ServiceStatusNew.PA_NOT_ELIGIBLE,
      -> result

      else -> result.copy(failureReasons = emptyList())
    }
  }

  /** Creates a terminal outcome node for NOT_ELIGIBLE status */
  fun notEligible(service: AccommodationService) = OutcomeNode("notEligible") { ctx -> toNotEligibleServiceStatus(service, ctx.currentResult.failureReasons) }
  fun notRequired(service: AccommodationService) = OutcomeNode("notRequired") { ctx -> toNotRequiredServiceStatus(service, ctx.currentResult.failureReasons) }

  /** Creates a terminal outcome node that returns the current context's ServiceResult with failure reasons. */
  fun currentOutcome() = OutcomeNode("currentOutcome") { ctx -> ctx.currentResult }
}
