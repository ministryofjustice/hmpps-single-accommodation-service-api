package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.TierRiskScores

class TierRiskScoresTest {

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["A", "B", "C"])
  fun `male high risk tiers include A, B and C tiers`(tierScore: TierScore) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["A", "B", "C"], mode = EnumSource.Mode.EXCLUDE)
  @NullSource
  fun `male high risk tiers exclude all tiers other than A, B or C`(tierScore: TierScore?) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["A", "B", "C", "D"])
  fun `non-male high risk tiers include A, B, C and D tiers`(tierScore: TierScore) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["A", "B", "C", "D"], mode = EnumSource.Mode.EXCLUDE)
  @NullSource
  fun `non-male high risk tiers exclude all tiers other than A, B, C and D`(tierScore: TierScore?) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["MISSING", "NOT_SUPERVISED"], mode = EnumSource.Mode.EXCLUDE)
  fun `is-single-letter-tier tiers include A-G tiers`(tierScore: TierScore) {
    assertThat(TierRiskScores.isSingleLetterTier(tierScore)).isTrue()
  }

  @ParameterizedTest
  @EnumSource(TierScore::class, names = ["MISSING", "NOT_SUPERVISED"])
  @NullSource
  fun `is-single-letter-tier tiers excludes MISSING, NOT_SUPERVISED`(tierScore: TierScore?) {
    assertThat(TierRiskScores.isSingleLetterTier(tierScore)).isFalse()
  }
}
