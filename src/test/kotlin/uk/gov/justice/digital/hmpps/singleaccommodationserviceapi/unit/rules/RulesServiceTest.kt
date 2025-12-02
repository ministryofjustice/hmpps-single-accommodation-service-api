package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus

class RulesServiceTest {
  private val rulesService = RulesService()

  @Test
  fun `calculate eligibility for cas 1`() {
    val crn = "X12345"
    val result = rulesService.calculateEligibilityForCas1(crn)
    val expectedResult = ServiceResult(listOf(), ServiceStatus.NOT_STARTED, "Start approved premise referral in 181 days")
    assertThat(result).isEqualTo(expectedResult)
  }
}
