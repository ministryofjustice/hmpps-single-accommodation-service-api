package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import java.util.UUID

class RulesServiceTest {
  private val rulesService = RulesService()

  @Nested
  inner class BuildEligibilityServiceResult {
    @Test
    fun `ruleset passes so returns UPCOMING with no failed results and an action`() {
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.PASS,
        results = listOf(
          RuleResult(
            description = "rule 1",
            actionable = false,
            ruleStatus = RuleStatus.PASS,
            potentialAction = "Action1",
          ),
        ),
      )
      val result = rulesService.buildEligibilityServiceResult(ruleSetResult)
      val expectedResult = ServiceResult(
        failedResults = listOf(),
        serviceStatus = ServiceStatus.UPCOMING.toString(),
        actions = listOf("Action1"),
      )
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset fails so returns NOT_ELIGIBLE with one failed result and an action`() {
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.FAIL,
        results = listOf(
          RuleResult(
            description = "rule 1",
            actionable = false,
            ruleStatus = RuleStatus.FAIL,
            potentialAction = "Action1",
          ),
        ),
      )
      val result = rulesService.buildEligibilityServiceResult(ruleSetResult)
      val expectedResult = ServiceResult(
        failedResults = listOf(
          RuleResult(description = "rule 1", ruleStatus = RuleStatus.FAIL, actionable = false, potentialAction = "Action1"),
        ),
        serviceStatus = ServiceStatus.NOT_ELIGIBLE.toString(),
        actions = listOf(),
      )
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset fails so returns NOT_STARTED with one failed actionable result and an action`() {
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.ACTION_NEEDED,
        results = listOf(
          RuleResult(
            description = "rule 1",
            actionable = true,
            ruleStatus = RuleStatus.FAIL,
            potentialAction = "Action1",
          ),
        ),
      )
      val result = rulesService.buildEligibilityServiceResult(ruleSetResult)
      val expectedResult = ServiceResult(
        failedResults = listOf(
          RuleResult(description = "rule 1", ruleStatus = RuleStatus.FAIL, actionable = true, potentialAction = "Action1"),
        ),
        serviceStatus = ServiceStatus.NOT_STARTED.toString(),
        actions = listOf("Action1"),
      )
      assertThat(result).isEqualTo(expectedResult)
    }
  }

  @Nested
  inner class BuildStatusServiceResult {
    @Test
    fun `ruleset passes so returns UPCOMING with no failed results and an action`() {
      val cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        status = ApprovedPremisesApplicationStatus.PlacementAllocated,
      )
      val result = rulesService.buildStatusServiceResult(cas1Application)
      val expectedResult = ServiceResult(
        failedResults = listOf(),
        serviceStatus = ApprovedPremisesApplicationStatus.PlacementAllocated.toString(),
        actions = listOf(),
      )
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
