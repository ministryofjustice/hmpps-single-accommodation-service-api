package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus

class RulesServiceTest {
  private val rulesService = RulesService()

  @Nested
  inner class CalculateEligibilityForCas1 {
    @Test
    fun `calculate eligibility for cas 1`() {
      val crn = "X12345"
      val result = rulesService.calculateEligibilityForCas1(crn)
      val expectedResult = ServiceResult(listOf(), ServiceStatus.UPCOMING, listOf("Start approved premise referral"))
      assertThat(result).isEqualTo(expectedResult)
    }
  }

  @Nested
  inner class BuildCas1Results {
    @Test
    fun `ruleset passes so returns UPCOMING with no failed results and an action`() {
      val ruleSetResult = RuleSetResult(
        results = listOf(
          RuleResult(
            description = "rule1",
            ruleStatus = RuleStatus.PASS,
            isGuidance = false,
            potentialAction = "Test",
          ),
        ),
        ruleSetStatus = RuleSetStatus.PASS,
      )
      val result = rulesService.buildCas1Results(ruleSetResult)
      val expectedResult = ServiceResult(listOf(), ServiceStatus.UPCOMING, listOf("Test"))
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset has guidance fail so returns NOT_STARTED`() {
      val ruleSetResult = RuleSetResult(
        results = listOf(
          RuleResult(
            description = "rule1",
            ruleStatus = RuleStatus.FAIL,
            isGuidance = true,
          ),
        ),
        ruleSetStatus = RuleSetStatus.GUIDANCE_FAIL,
      )
      val result = rulesService.buildCas1Results(ruleSetResult)
      val expectedResult = ServiceResult(
        ruleSetResult.results,
        ServiceStatus.NOT_STARTED,
        listOf(),
      )
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset fails so returns NOT_ELIGIBLE and never has actions`() {
      val ruleSetResult = RuleSetResult(
        results = listOf(
          RuleResult(
            description = "rule1",
            ruleStatus = RuleStatus.FAIL,
            isGuidance = false,
            potentialAction = "TEST",
          ),
        ),
        ruleSetStatus = RuleSetStatus.FAIL,
      )
      val result = rulesService.buildCas1Results(ruleSetResult)
      val expectedResult = ServiceResult(
        ruleSetResult.results,
        ServiceStatus.NOT_ELIGIBLE,
        listOf(),
      )
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
