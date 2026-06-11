package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

/**
 * A provider that supplies one decision tree and the initial
 * EvaluationContext to evaluate it against.
 *
 * Each implementation owns the wiring of
 * 1. its own rulesets
 * 2. context updaters
 * 3. outcome nodes
 *
 * Each implementation is responsible for building its tree once (typically
 * lazily)
 */
interface EligibilityTreeProvider {
  fun tree(): DecisionNode

  /** The initial [EvaluationContext] to evaluate the tree against for [data]. */
  fun initialContext(data: DomainData): EvaluationContext

  fun resolveDeeplink(result: ServiceResult, data: DomainData): ServiceResult = result
}
