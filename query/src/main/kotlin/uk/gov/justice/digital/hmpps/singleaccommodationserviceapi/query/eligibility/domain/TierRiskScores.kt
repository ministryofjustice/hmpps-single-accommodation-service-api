package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

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

  private val singleLetterTiers = setOf(
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
  )

  fun isSingleLetterTier(tierScore: String?) = tierScore in singleLetterTiers

  fun isHighRiskForMale(tierScore: String?) = tierScore in maleHighRiskTiers

  fun isHighRiskForNonMale(tierScore: String?) = tierScore in nonMaleHighRiskTiers
}
