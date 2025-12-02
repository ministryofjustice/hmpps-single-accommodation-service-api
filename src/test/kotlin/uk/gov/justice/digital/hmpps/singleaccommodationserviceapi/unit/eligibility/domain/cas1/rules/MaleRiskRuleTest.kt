package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityBaseTest
import java.time.OffsetDateTime
import java.util.stream.Stream

class MaleRiskRuleTest : EligibilityBaseTest() {
  private val crn = "ABC234"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules.MaleRiskRuleTest#provideSexAndTierToPass")
  fun `candidate passes`(sex: Sex, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = maleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules.MaleRiskRuleTest#provideSexAndTierToFail")
  fun `candidate fails`(sex: Sex, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = maleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = maleRiskRule.description
    assertThat(result).isEqualTo("FAIL if candidate is Male and is not Tier A3 - B1")
  }

  private companion object {

    private val female = Sex(
      code = SexCode.F,
      description = "Female",
    )
    private val male = Sex(
      code = SexCode.M,
      description = "Male",
    )
    private val notRecorded = Sex(
      code = SexCode.N,
      description = "Not Known / Not Recorded",
    )
    private val notSpecified = Sex(
      code = SexCode.NS,
      description = "Not Specified",
    )

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
        Arguments.of(female, it)
      }
      val notRecordedArguments = allTiers.map {
        Arguments.of(notRecorded, it)
      }
      val notSpecifiedArguments = allTiers.map {
        Arguments.of(notSpecified, it)
      }
      val maleArguments = highRiskTiers.map {
        Arguments.of(male, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments + maleArguments).stream()
    }

    @JvmStatic
    fun provideSexAndTierToFail() = lowRiskTiers.map {
      Arguments.of(male, it)
    }.stream()
  }
}
