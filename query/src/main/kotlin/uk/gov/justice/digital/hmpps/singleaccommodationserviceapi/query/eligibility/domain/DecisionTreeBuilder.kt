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
    name: String,
    ruleSet: RuleSet,
  ) = RuleSetNodeBuilder(
    name = name,
    ruleSet = ruleSet,
    engine = engine,
  )

  /** Creates a terminal outcome node that returns a fixed ServiceResult. */
  fun outcome(name: String, result: ServiceResultNew) = OutcomeNode(name = "return status ${result.serviceStatus.name}") { _ -> result }

  /** Creates a terminal outcome node that returns the current context's ServiceResult. */
  fun confirmed() = OutcomeNode("return current status") { ctx ->
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
  fun notEligible(service: ServiceType) = OutcomeNode("return status ${toNotEligibleServiceStatus(service)}") { ctx -> toNotEligibleServiceResult(service, ctx.currentResult.failureReasons) }
  fun notRequired(service: ServiceType) = OutcomeNode("return status ${toNotRequiredServiceStatus(service)}") { ctx -> toNotRequiredServiceResult(service, ctx.currentResult.failureReasons) }

  /** Creates a terminal outcome node that returns the current context's ServiceResult with failure reasons. */
  fun currentOutcome() = OutcomeNode("return current status (deprecated version)") { ctx -> ctx.currentResult }
}
