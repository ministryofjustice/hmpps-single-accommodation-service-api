package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.client.tier

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper

class TierTest {
  @ParameterizedTest
  @EnumSource(TierScore::class)
  fun `tier scores serialize as strings and deserialize from upstream responses`(tierScore: TierScore) {
    val tier = buildTier(tierScore)
    val json = jsonMapper.writeValueAsString(tier)

    assertThat(jsonMapper.readTree(json).get("tierScore").asString()).isEqualTo(tierScore.name)
    assertThat(jsonMapper.readValue(json, Tier::class.java)).isEqualTo(tier)
  }

  @ParameterizedTest
  @ValueSource(strings = ["A1", "B2", "A3S", "a", "UNKNOWN"])
  fun `unsupported tier scores are rejected`(tierScore: String) {
    val json = jsonMapper.writeValueAsString(buildTier()).replace("\"A\"", "\"$tierScore\"")

    assertThatThrownBy { jsonMapper.readValue(json, Tier::class.java) }
      .hasMessageContaining(tierScore)
  }
}
