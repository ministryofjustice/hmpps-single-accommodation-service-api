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
  private val description = "FAIL if candidate is not Male and is not Tier A, B, C or D"

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
    assertThat(result).isEqualTo("FAIL if candidate is not Male and is not Tier A, B, C or D")
  }

  private companion object {

    val nonMalePassingTiers = listOf("A", "B", "C", "D")
    val nonMaleFailingTiers = listOf("E", "F", "G", "MISSING", "NOT_SUPERVISED")
    val allTiers = listOf("A", "B", "C", "D", "E", "F", "G", "MISSING", "NOT_SUPERVISED")

    @JvmStatic
    fun provideSexAndTierToPass(): Stream<Arguments> {
      val femaleArguments = nonMalePassingTiers.map {
        Arguments.of(SexCode.F, it)
      }
      val notRecordedArguments = nonMalePassingTiers.map {
        Arguments.of(SexCode.N, it)
      }
      val notSpecifiedArguments = nonMalePassingTiers.map {
        Arguments.of(SexCode.NS, it)
      }
      val maleArguments = allTiers.map {
        Arguments.of(SexCode.M, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments + maleArguments).stream()
    }

    @JvmStatic
    fun provideSexAndTierToFail(): Stream<Arguments> {
      val femaleArguments = nonMaleFailingTiers.map {
        Arguments.of(SexCode.F, it)
      }
      val notRecordedArguments = nonMaleFailingTiers.map {
        Arguments.of(SexCode.N, it)
      }
      val notSpecifiedArguments = nonMaleFailingTiers.map {
        Arguments.of(SexCode.NS, it)
      }
      return (femaleArguments + notRecordedArguments + notSpecifiedArguments).stream()
    }
  }
}
