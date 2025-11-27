package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule

class STierRuleTest {
  @Test
  fun `candidate is not S tier`() {
    val data = DomainData("A1")
    val result = STierRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate is S tier`() {
    val data = DomainData("B2S")
    val result = STierRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule is applicable to CAS 1`() {
    val result = STierRule().services

    assertThat(result).contains(ServiceType.CAS1)
  }


}