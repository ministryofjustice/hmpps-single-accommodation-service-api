package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.FemaleRiskRule
import java.time.OffsetDateTime

class FemaleRiskRuleTest {
  private val femaleRiskRule = FemaleRiskRule()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )

  @Test
  fun `candidate is not female so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = femaleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "A3",
      "A2",
      "A1S",
      "B3",
      "B2",
      "B1S",
      "C3",
      "C3S",
    ],
  )
  fun `candidate is female and within tiers A3-C3 so passes`(tier: String) {
    val data = DomainData(
      tier = tier,
      sex = female,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = femaleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "C2",
      "C1S",
      "D3S",
      "D2",
      "D1",
    ],
  )
  fun `candidate is female and outside tiers A3-C3 so fails`(tier: String) {
    val data = DomainData(
      tier = tier,
      sex = female,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = femaleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule is applicable to CAS 1`() {
    val result = femaleRiskRule.services
    assertThat(result).contains(ServiceType.CAS1)
  }

  @Test
  fun `rule has correct description`() {
    val result = femaleRiskRule.description
    assertThat(result).isEqualTo("FAIL if candidate is Female and is not Tier A3 - C3")
  }
}
