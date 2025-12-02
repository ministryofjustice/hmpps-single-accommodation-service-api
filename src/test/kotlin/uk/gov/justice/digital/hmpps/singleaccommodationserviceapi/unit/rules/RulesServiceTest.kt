package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService

class RulesServiceTest {
  private val rulesService = RulesService()

//  @Nested
//  inner class BuildCas1Results {
//    @Test
//    fun `ruleset passes so returns UPCOMING with no failed results and an action`() {
//      val ruleSetResult = RuleSetResult(
//        results = listOf(
//          RuleResult(
//            description = "rule1",
//            ruleStatus = RuleStatus.PASS,
//            isGuidance = false,
//            potentialAction = "Test",
//          ),
//        ),
//        ruleSetStatus = RuleSetStatus.PASS,
//      )
//      val result = rulesService.buildResults(ruleSetResult, data)
//      val expectedResult = ServiceResult(listOf(), ServiceStatus.UPCOMING, listOf("Test"))
//      assertThat(result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun `ruleset has guidance fail so returns NOT_STARTED`() {
//      val ruleSetResult = RuleSetResult(
//        results = listOf(
//          RuleResult(
//            description = "rule1",
//            ruleStatus = RuleStatus.FAIL,
//            isGuidance = true,
//          ),
//        ),
//        ruleSetStatus = RuleSetStatus.GUIDANCE_FAIL,
//      )
//      val result = rulesService.buildCas1EligibilityResults(ruleSetResult)
//      val expectedResult = ServiceResult(
//        ruleSetResult.results,
//        ServiceStatus.NOT_STARTED,
//        listOf(),
//      )
//      assertThat(result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun `ruleset fails so returns NOT_ELIGIBLE and never has actions`() {
//      val ruleSetResult = RuleSetResult(
//        results = listOf(
//          RuleResult(
//            description = "rule1",
//            ruleStatus = RuleStatus.FAIL,
//            isGuidance = false,
//            potentialAction = "TEST",
//          ),
//        ),
//        ruleSetStatus = RuleSetStatus.FAIL,
//      )
//      val result = rulesService.buildCas1EligibilityResults(ruleSetResult)
//      val expectedResult = ServiceResult(
//        ruleSetResult.results,
//        ServiceStatus.NOT_ELIGIBLE,
//        listOf(),
//      )
//      assertThat(result).isEqualTo(expectedResult)
//    }
//  }
}
