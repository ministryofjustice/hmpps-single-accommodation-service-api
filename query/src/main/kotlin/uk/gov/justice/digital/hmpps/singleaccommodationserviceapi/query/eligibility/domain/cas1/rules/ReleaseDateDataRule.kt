package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class ReleaseDateDataRule : Cas1DataRule {
  override val description = "FAIL if candidate has no release date"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.releaseDate == null) {
      RuleStatus.FAIL
    } else {
      RuleStatus.PASS
    },
  )
}
