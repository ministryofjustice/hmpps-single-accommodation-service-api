package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

interface Rule<in D> {
  val inputFields: Set<DomainDataField>
  val description: String
  fun evaluate(data: D): RuleResult
  val failureReason: FailureReason? get() = null
}
