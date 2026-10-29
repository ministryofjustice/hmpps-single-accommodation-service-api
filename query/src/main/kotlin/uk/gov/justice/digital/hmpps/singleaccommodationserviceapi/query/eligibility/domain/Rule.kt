package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason

interface Rule {
  val description: String
  fun evaluate(data: DomainData): RuleResult
  val failureReason: FailureReason?
}
