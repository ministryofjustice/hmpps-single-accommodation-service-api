package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew

/**
 * Evaluation context that carries state through decision tree traversal.
 * Contains the domain data and the current best-known ServiceResult.
 *
 */
data class EvaluationContext(
  val data: DomainData,
  /**
   * Will be `null` until a [ContextUpdater] sets a concrete status
   */
  val currentResult: ServiceResultNew?,
)
