package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

/**
 * Evaluation context that carries state through decision tree traversal.
 * Contains the domain data and the current best-known ServiceResult.
 */
data class EvaluationContext(
  val data: DomainData,
  val currentResult: ServiceResult
)