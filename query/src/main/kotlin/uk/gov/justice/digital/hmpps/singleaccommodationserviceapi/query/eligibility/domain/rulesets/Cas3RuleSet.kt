package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ReferralNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3ApplicationData

@Component
class Cas3RuleSet(
  cas3ReferralNotActive: Cas3ReferralNotActiveRule,
  cas3NotPresent: Cas3NotPresentRule,
  cas3BookingNotActive: Cas3BookingNotActiveRule,
  cas3AssessmentNotActive: Cas3AssessmentNotActiveRule,
) : RuleSet<Cas3ApplicationData> {
  private val rules: List<Rule<Cas3ApplicationData>> = listOf(
    cas3ReferralNotActive,
    cas3NotPresent,
    cas3BookingNotActive,
    cas3AssessmentNotActive,
  )

  override val name: String = "CAS3 Referral"

  override fun getRules(): List<Rule<Cas3ApplicationData>> = rules
}
