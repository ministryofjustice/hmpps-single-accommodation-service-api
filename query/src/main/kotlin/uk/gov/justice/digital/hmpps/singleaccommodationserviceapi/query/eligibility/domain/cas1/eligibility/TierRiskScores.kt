package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

internal object TierRiskScores {

  private val maleHighRiskTiers = setOf(
    "A",
    "B",
    "C",
  )
  private val nonMaleHighRiskTiers = setOf(
    "A",
    "B",
    "C",
    "D",
  )

  fun isHighRiskForMale(tierScore: String?) = tierScore in maleHighRiskTiers

  fun isHighRiskForNonMale(tierScore: String?) = tierScore in nonMaleHighRiskTiers
}
