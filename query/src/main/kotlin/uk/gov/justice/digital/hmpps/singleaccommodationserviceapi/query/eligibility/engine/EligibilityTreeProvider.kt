package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.IdentifiedData

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
interface EligibilityTreeProvider<D : IdentifiedData> {
  fun tree(): DecisionNode<D>

  /** The initial [EvaluationContext] to evaluate the tree against for [data]. */
  fun initialContext(data: D): EvaluationContext<D>

  /** The existing application link, read only from this service's inputs. */
  fun applicationUrl(data: D): String? = null
}
