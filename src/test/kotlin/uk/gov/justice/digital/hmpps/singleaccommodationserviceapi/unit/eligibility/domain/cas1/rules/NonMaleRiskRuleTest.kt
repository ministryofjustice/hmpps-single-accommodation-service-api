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

class NonMaleRiskRuleTest : EligibilityBaseTest() {

  private val crn = "ABC234"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules.NonMaleRiskRuleTest#provideSexAndTierToPass")
  fun `candidate passes`(sex: Sex, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = nonMaleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules.NonMaleRiskRuleTest#provideSexAndTierToFail")
  fun `candidate fails`(sex: Sex, tier: TierScore) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = sex,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = nonMaleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = nonMaleRiskRule.description
    assertThat(result).isEqualTo("FAIL if candidate is not Male and is not Tier A3 - C3")
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
      code = SexCode.N,
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
      TierScore.C3,
      TierScore.C3S,
    )

    private val lowRiskTiers = listOf(
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
      val femaleArguments = highRiskTiers.map {
        Arguments.of(female, it)
      }
      val notRecordedArguments = highRiskTiers.map {
        Arguments.of(notRecorded, it)
      }
      val notSpecifiedArguments = highRiskTiers.map {
        Arguments.of(notSpecified, it)
      }
      val maleArguments = allTiers.map {
        Arguments.of(male, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments + maleArguments).stream()
    }

    @JvmStatic
    fun provideSexAndTierToFail(): Stream<Arguments> {
      val femaleArguments = lowRiskTiers.map {
        Arguments.of(female, it)
      }
      val notRecordedArguments = lowRiskTiers.map {
        Arguments.of(notRecorded, it)
      }
      val notSpecifiedArguments = lowRiskTiers.map {
        Arguments.of(notSpecified, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments).stream()
    }
  }
}
