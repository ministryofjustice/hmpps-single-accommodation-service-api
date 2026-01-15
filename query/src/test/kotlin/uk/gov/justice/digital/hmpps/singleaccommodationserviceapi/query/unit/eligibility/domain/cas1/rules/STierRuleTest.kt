package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.STierEligibilityRule
import java.time.LocalDate

class STierRuleTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.STierRuleTest#provideNonSTierToPass")
  fun `candidate passes`(tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = STierEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.STierRuleTest#provideSTierToFail")
  fun `candidate fails`(tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = STierEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = STierEligibilityRule().description
    assertThat(result).isEqualTo("FAIL if candidate is S Tier")
  }

  private companion object {

    private val nonSTiers = listOf(
      TierScore.A3,
      TierScore.A2,
      TierScore.A1,
      TierScore.B3,
      TierScore.B2,
      TierScore.B1,
      TierScore.C3,
      TierScore.C2,
      TierScore.C1,
      TierScore.D3,
      TierScore.D2,
      TierScore.D1,
    )

    private val sTiers = listOf(
      TierScore.A3S,
      TierScore.A2S,
      TierScore.A1S,
      TierScore.B3S,
      TierScore.B2S,
      TierScore.B1S,
      TierScore.C3S,
      TierScore.C2S,
      TierScore.C1S,
      TierScore.D3S,
      TierScore.D2S,
      TierScore.D1S,
    )

    @JvmStatic
    fun provideNonSTierToPass() = nonSTiers.map {
      Arguments.of(it)
    }.stream()

    @JvmStatic
    fun provideSTierToFail() = sTiers.map {
      Arguments.of(it)
    }.stream()
  }
}
