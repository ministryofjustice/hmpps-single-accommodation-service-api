package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore

internal object TierRiskScores {

  private val maleHighRiskTiers = setOf(
    TierScore.A,
    TierScore.B,
    TierScore.C,
  )
  private val nonMaleHighRiskTiers = setOf(
    TierScore.A,
    TierScore.B,
    TierScore.C,
    TierScore.D,
  )

  private val singleLetterTiers = setOf(
    TierScore.A,
    TierScore.B,
    TierScore.C,
    TierScore.D,
    TierScore.E,
    TierScore.F,
    TierScore.G,
  )

  fun isSingleLetterTier(tierScore: TierScore?) = tierScore in singleLetterTiers

  fun isHighRiskForMale(tierScore: TierScore?) = tierScore in maleHighRiskTiers

  fun isHighRiskForNonMale(tierScore: TierScore?) = tierScore in nonMaleHighRiskTiers
}
