package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData

@Component
class PpcPracticalSupportSufficientRule : Rule {
  override val description = "FAIL if practical support sufficient"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (data.ppcAnswers?.practicalSupportSufficient == true) RuleStatus.FAIL else RuleStatus.PASS,
  )
}
