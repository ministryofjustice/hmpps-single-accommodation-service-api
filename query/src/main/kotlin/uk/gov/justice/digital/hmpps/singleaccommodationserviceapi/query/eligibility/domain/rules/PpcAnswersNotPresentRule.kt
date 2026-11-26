package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class PpcAnswersNotPresentRule : Rule {
  override val description = "FAIL if PPC answers have not been submitted"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.publicProtectionChecker == null) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
