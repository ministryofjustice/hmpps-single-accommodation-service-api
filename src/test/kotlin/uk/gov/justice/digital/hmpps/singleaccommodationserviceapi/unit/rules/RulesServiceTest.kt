package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.FinalResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus

class RulesServiceTest {
  private val rulesService = RulesService()

  @Test
  fun `calculate eligibility for cas 1`() {
    val crn = "X12345"
    val result = rulesService.calculateEligibilityForCas1(crn)
    val expectedResult = FinalResult(listOf(), RuleSetStatus.PASS)
    assertThat(result).isEqualTo(expectedResult)
  }
}
