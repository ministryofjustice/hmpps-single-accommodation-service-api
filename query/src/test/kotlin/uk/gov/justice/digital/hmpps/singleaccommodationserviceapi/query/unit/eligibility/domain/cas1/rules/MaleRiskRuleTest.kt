package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.MaleRiskEligibilityRule
import java.time.LocalDate
import java.util.stream.Stream

class MaleRiskRuleTest {
  private val crn = "ABC234"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.MaleRiskRuleTest#provideSexAndTierToPass")
  fun `candidate passes`(sex: SexCode, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = MaleRiskEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.MaleRiskRuleTest#provideSexAndTierToFail")
  fun `candidate fails`(sex: SexCode, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = MaleRiskEligibilityRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = MaleRiskEligibilityRule().description
    assertThat(result).isEqualTo("FAIL if candidate is Male and is not Tier A3 - B1")
  }

  private companion object {

    private val highRiskTiers = listOf(
      TierScore.A3,
      TierScore.A2,
      TierScore.A1,
      TierScore.A3S,
      TierScore.A2S,
      TierScore.A1S,
      TierScore.B3,
      TierScore.B2,
      TierScore.B1,
      TierScore.B3S,
      TierScore.B2S,
      TierScore.B1S,
    )

    private val lowRiskTiers = listOf(
      TierScore.C3,
      TierScore.C3S,
      TierScore.C2,
      TierScore.C1,
      TierScore.C2S,
      TierScore.C1S,
      TierScore.D3,
      TierScore.D2,
      TierScore.D1,
      TierScore.D3S,
      TierScore.D2S,
      TierScore.D1S,
    )

    private val allTiers = highRiskTiers + lowRiskTiers

    @JvmStatic
    fun provideSexAndTierToPass(): Stream<Arguments> {
      val femaleArguments = allTiers.map {
        Arguments.of(SexCode.F, it)
      }
      val notRecordedArguments = allTiers.map {
        Arguments.of(SexCode.N, it)
      }
      val notSpecifiedArguments = allTiers.map {
        Arguments.of(SexCode.NS, it)
      }
      val maleArguments = highRiskTiers.map {
        Arguments.of(SexCode.M, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments + maleArguments).stream()
    }

    @JvmStatic
    fun provideSexAndTierToFail() = lowRiskTiers.map {
      Arguments.of(SexCode.M, it)
    }.stream()
  }
}
