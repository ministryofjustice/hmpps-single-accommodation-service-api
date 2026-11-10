package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

interface Rule {
  val description: String
  fun evaluate(data: DomainData): RuleResult
  val failureReason: FailureReason? get() = null
}
