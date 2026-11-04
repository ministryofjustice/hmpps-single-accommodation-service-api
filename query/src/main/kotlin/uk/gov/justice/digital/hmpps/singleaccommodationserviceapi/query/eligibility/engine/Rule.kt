package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason

interface Rule {
  val description: String
  fun evaluate(data: DomainData): RuleResult
  val failureReason: FailureReason? get() = null
}
