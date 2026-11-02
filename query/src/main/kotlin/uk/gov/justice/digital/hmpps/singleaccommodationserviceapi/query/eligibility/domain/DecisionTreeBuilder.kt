package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotEligibleServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toNotRequiredServiceResult
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
  fun ruleSet(
    ruleSet: RuleSet,
  ) = RuleSetNodeBuilder(
    name = ruleSet.name,
    ruleSet = ruleSet,
    engine = engine,
  )

  /** Creates a terminal outcome node that returns a fixed ServiceResult. */
  fun outcome(result: ServiceResultNew) = OutcomeNode(name = result.serviceStatus.uiStatusName, key = result.serviceStatus.name) { _ -> result }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed() = OutcomeNode(name = "current status", key = "confirmed") { ctx ->
    val result = requireNotNull(ctx.currentResult) {
      "confirmed() reached without a currentResult being set - a context updater is missing earlier in the tree"
    }
    when (result.serviceStatus) {
      ServiceStatusNew.CAS1_NOT_ELIGIBLE,
      ServiceStatusNew.CAS2_NOT_ELIGIBLE,
      ServiceStatusNew.CAS3_NOT_ELIGIBLE,
      ServiceStatusNew.CRS_NOT_REQUIRED,
      ServiceStatusNew.DTR_NOT_REQUIRED,
      ServiceStatusNew.CRS_NOT_ELIGIBLE,
      ServiceStatusNew.DTR_NOT_ELIGIBLE,
      ServiceStatusNew.PA_NOT_ELIGIBLE,
      ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION,
      ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS,
      ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION,
      ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_CRS,
      ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR,
      -> result

      else -> result.copy(failureReasons = emptyList())
    }
  }

  /** Creates a terminal outcome node for NOT_ELIGIBLE status */
  fun notEligible(service: ServiceType) = toNotEligibleServiceStatus(service).let { status ->
    OutcomeNode(name = status.uiStatusName, key = status.name) { ctx -> toNotEligibleServiceResult(service, ctx.currentResult?.failureReasons ?: ctx.failureReasons) }
  }
  fun notRequired(service: ServiceType) = toNotRequiredServiceStatus(service).let { status ->
    OutcomeNode(name = status.uiStatusName, key = status.name) { ctx -> toNotRequiredServiceResult(service, ctx.currentResult?.failureReasons ?: ctx.failureReasons) }
  }
}
