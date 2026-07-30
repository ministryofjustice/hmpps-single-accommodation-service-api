package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.stream.Stream

class NonMaleRiskRuleTest {
  private val description = "FAIL if candidate is not Male and is not Tier A3 - C3"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility.NonMaleRiskRuleTest#provideSexAndTierToPass")
  fun `candidate passes`(sex: SexCode, tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
      sex = sex,
    )

    val result = NonMaleRiskEligibilityRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility.NonMaleRiskRuleTest#provideSexAndTierToFail")
  fun `candidate fails`(sex: SexCode, tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
      sex = sex,
    )

    val result = NonMaleRiskEligibilityRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.NON_MALE_NOT_HIGH_RISK_TIER))
  }

  @Test
  fun `rule has correct description`() {
    val result = NonMaleRiskEligibilityRule().description
    assertThat(result).isEqualTo("FAIL if candidate is not Male and is not Tier A3 - C3")
  }

  private companion object {

    private val highRiskTiers = listOf(
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

    private val lowRiskTiers = listOf(
      "C2",
      "C1",
      "C2S",
      "C1S",
      "D3",
      "D2",
      "D1",
      "D3S",
      "D2S",
      "D1S",
    )

    private val allTiers = highRiskTiers + lowRiskTiers

    @JvmStatic
    fun provideSexAndTierToPass(): Stream<Arguments> {
      val femaleArguments = highRiskTiers.map {
        Arguments.of(SexCode.F, it)
      }
      val notRecordedArguments = highRiskTiers.map {
        Arguments.of(SexCode.N, it)
      }
      val notSpecifiedArguments = highRiskTiers.map {
        Arguments.of(SexCode.NS, it)
      }
      val maleArguments = allTiers.map {
        Arguments.of(SexCode.M, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments + maleArguments).stream()
    }

    @JvmStatic
    fun provideSexAndTierToFail(): Stream<Arguments> {
      val femaleArguments = lowRiskTiers.map {
        Arguments.of(SexCode.F, it)
      }
      val notRecordedArguments = lowRiskTiers.map {
        Arguments.of(SexCode.N, it)
      }
      val notSpecifiedArguments = lowRiskTiers.map {
        Arguments.of(SexCode.NS, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments).stream()
    }
  }
}
