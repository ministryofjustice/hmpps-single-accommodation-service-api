package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility

internal object TierRiskScores {

  private val maleHighRiskTiers = setOf(
    "A3",
    "A2",
    "A1",
    "A3S",
    "A2S",
    "A1S",
    "B3",
    "B2",
    "B1",
    "B3S",
    "B2S",
    "B1S",
  )
  private val nonMaleHighRiskTiers = setOf(
    "A3",
    "A2",
    "A1",
    "A3S",
    "A2S",
    "A1S",
    "B3",
    "B2",
    "B1",
    "B3S",
    "B2S",
    "B1S",
    "C3",
    "C3S",
  )

  fun isHighRiskForMale(tierScore: String?) = tierScore in maleHighRiskTiers

  fun isHighRiskForNonMale(tierScore: String?) = tierScore in nonMaleHighRiskTiers

  fun isSTier(tierScore: String?) = tierScore?.endsWith("S") == true
}
