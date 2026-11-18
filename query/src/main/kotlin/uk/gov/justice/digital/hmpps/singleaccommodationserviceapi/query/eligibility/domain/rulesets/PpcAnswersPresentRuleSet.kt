package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PpcAnswersNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class PpcAnswersPresentRuleSet(
  private val ppcAnswersNotPresentRule: PpcAnswersNotPresentRule,
) : RuleSet {
  override val name = "PASS if PPC answers have been submitted"

  override fun getRules(): List<Rule> = listOf(ppcAnswersNotPresentRule)
}
