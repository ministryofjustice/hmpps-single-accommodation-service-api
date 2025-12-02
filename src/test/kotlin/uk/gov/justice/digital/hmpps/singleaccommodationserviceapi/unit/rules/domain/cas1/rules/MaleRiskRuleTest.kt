package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.MaleRiskRule
import java.time.OffsetDateTime

class MaleRiskRuleTest {
  private val maleRiskRule = MaleRiskRule()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )

  @Test
  fun `candidate is not male and passes`() {
    val data = DomainData(
      tier = "A1",
      sex = female,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = maleRiskRule.evaluate(data)

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
    ],
  )
  fun `candidate is male and within tiers A3-B1 and passes`(tier: String) {
    val data = DomainData(
      tier = tier,
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )

    val result = maleRiskRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "C3",
      "C2",
      "C1S",
      "D3S",
      "D2",
      "D1",
    ],
  )
  fun `candidate is male and outside tiers A3-B1 and fails`(tier: String) {
    val data = DomainData(
      tier = tier,
      sex = male,
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
}
