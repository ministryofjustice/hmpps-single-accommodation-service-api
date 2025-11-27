package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule
import java.time.OffsetDateTime

class STierRuleTest {
  private val sTierRule = STierRule()
  private val male = Sex(
    code = "M",
    description = "Male",
  )

  @Test
  fun `candidate is not S tier so passes`() {
    val data = DomainData(
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
      tier = "B2S",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )
    val result = sTierRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule is applicable to CAS 1`() {
    val result = sTierRule.services
    assertThat(result).contains(ServiceType.CAS1)
  }

  @Test
  fun `rule has correct description`() {
    val result = sTierRule.description
    assertThat(result).isEqualTo("FAIL if candidate is S Tier")
  }
}
