package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRuleNonMale

@Component
class CrsCompletionRuleSet(
  @Qualifier("crsSubmittedRule") crsSubmitted: CrsSubmittedRule,
  @Qualifier("crsExpiredRule") crsExpired: CrsExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(crsSubmitted, crsExpired)
  override fun getRules(): List<Rule> = rules
}

@Component
class CrsCompletionRuleSetMale(
  @Qualifier("crsSubmittedRuleMale") crsSubmitted: CrsSubmittedRuleMale,
  @Qualifier("crsExpiredRuleMale") crsExpired: CrsExpiredRuleMale,
) : CrsCompletionRuleSet(crsSubmitted, crsExpired)

@Component
class CrsCompletionRuleSetNonMale(
  @Qualifier("crsSubmittedRuleNonMale") crsSubmitted: CrsSubmittedRuleNonMale,
  @Qualifier("crsExpiredRuleNonMale") crsExpired: CrsExpiredRuleNonMale,
) : CrsCompletionRuleSet(crsSubmitted, crsExpired)
