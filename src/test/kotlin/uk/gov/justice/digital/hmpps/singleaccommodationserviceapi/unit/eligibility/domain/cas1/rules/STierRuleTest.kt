package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import java.time.OffsetDateTime

class STierRuleTest {
  private val sTierRule = STierRule()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val crn = "ABC234"

  @Test
  fun `candidate is not S tier so passes`() {
    val data = DomainData(
      crn = crn,
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )
    val result = sTierRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate is S tier so fails`() {
    val data = DomainData(
      crn = crn,
      tier = "B2S",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )
    val result = sTierRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = sTierRule.description
    assertThat(result).isEqualTo("FAIL if candidate is S Tier")
  }
}
