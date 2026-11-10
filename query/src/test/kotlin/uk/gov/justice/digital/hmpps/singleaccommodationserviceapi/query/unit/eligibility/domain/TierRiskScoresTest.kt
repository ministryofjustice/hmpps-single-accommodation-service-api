package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.TierRiskScores

class TierRiskScoresTest {

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C"])
  fun `male high risk tiers include A, B and C tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["D", "E", "F", "G", "MISSING", "NOT_SUPERVISED"])
  @NullSource
  fun `male high risk tiers exclude all tiers other than A, B or C`(tierScore: String?) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C", "D"])
  fun `non-male high risk tiers include A, B, C and D tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["E", "F", "G", "MISSING", "NOT_SUPERVISED"])
  @NullSource
  fun `non-male high risk tiers exclude all tiers other than A, B, C and D`(tierScore: String?) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["A", "B", "C", "D", "E", "F", "G"])
  fun `is-single-letter-tier tiers include A-G tiers`(tierScore: String) {
    assertThat(TierRiskScores.isSingleLetterTier(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["MISSING", "NOT_SUPERVISED"])
  @NullSource
  fun `is-single-letter-tier tiers excludes MISSING, NOT_SUPERVISED`(tierScore: String?) {
    assertThat(TierRiskScores.isSingleLetterTier(tierScore)).isFalse()
  }
}
