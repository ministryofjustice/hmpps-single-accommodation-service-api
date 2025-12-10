package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule

class Cas1RuleSet(
  private val sTierRule: STierRule,
  private val maleRiskRule: MaleRiskRule,
  private val nonMaleRiskRule: NonMaleRiskRule,
  private val withinSixMonthsOfReleaseRule: WithinSixMonthsOfReleaseRule,
) : RuleSet {
  override fun getRules(): List<Rule> = listOf(
    sTierRule,
    maleRiskRule,
    nonMaleRiskRule,
    withinSixMonthsOfReleaseRule,
  )
}
