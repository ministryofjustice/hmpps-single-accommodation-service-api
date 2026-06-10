package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.TierRiskScores

class TierRiskScoresTest {

  @ParameterizedTest
  @ValueSource(strings = ["A3", "A2", "A1", "A3S", "A2S", "A1S", "B3", "B2", "B1", "B3S", "B2S", "B1S"])
  fun `male high risk tiers include A and B tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["C3", "C3S", "C2", "C1", "D3", "D2", "D1"])
  fun `male high risk tiers exclude C D and unknown tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["A3", "A2", "A1", "B3", "B2", "B1", "C3", "A3S", "B3S", "C3S"])
  fun `non-male high risk tiers include A B and C3 tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["C2", "C1", "D3", "D2", "D1"])
  fun `non-male high risk tiers exclude lower C D and unknown tiers`(tierScore: String) {
    assertThat(TierRiskScores.isHighRiskForNonMale(tierScore)).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["A3S", "B1S", "C2S", "D1S", "Z9S"])
  fun `S tiers are scores ending with S`(tierScore: String) {
    assertThat(TierRiskScores.isSTier(tierScore)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["A3", "B1", "C2", "D1", "Z9"])
  fun `non-S tiers are scores not ending with S`(tierScore: String) {
    assertThat(TierRiskScores.isSTier(tierScore)).isFalse()
  }
}
