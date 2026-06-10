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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.stream.Stream

class MaleRiskRuleTest {
  private val description = "FAIL if candidate is Male and is not Tier A3 - B1"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility.MaleRiskRuleTest#provideSexAndTierToPass")
  fun `candidate passes`(sex: SexCode, tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
      sex = sex,
    )

    val result = MaleRiskEligibilityRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility.MaleRiskRuleTest#provideSexAndTierToFail")
  fun `candidate fails`(sex: SexCode, tierScore: String) {
    val data = buildDomainData(
      tierScore = tierScore,
      sex = sex,
    )

    val result = MaleRiskEligibilityRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.MALE_NOT_HIGH_RISK_TIER))
  }

  @Test
  fun `rule has correct description`() {
    val result = MaleRiskEligibilityRule().description
    assertThat(result).isEqualTo("FAIL if candidate is Male and is not Tier A3 - B1")
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
    )

    private val lowRiskTiers = listOf(
      "C3",
      "C3S",
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
