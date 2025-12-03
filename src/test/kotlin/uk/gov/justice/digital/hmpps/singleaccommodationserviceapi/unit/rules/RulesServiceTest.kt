package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import java.time.OffsetDateTime
import java.util.UUID

class RulesServiceTest {
  private val rulesService = RulesService()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )

  @Nested
  inner class BuildResults {
    @Test
    fun `ruleset passes so returns passStatus with no failed results and an action`() {
      val data = DomainData(
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
        tier = "A1",
      )
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.PASS,
        results = listOf(
          RuleResult(
            description = "rule 1",
            isGuidance = false,
            ruleStatus = RuleStatus.PASS,
            potentialAction = "Action1",
          ),
        ),
      )
      val passStatus = ServiceStatus.CONFIRMED
      val guidanceFailStatus = ServiceStatus.UPCOMING
      val failFunc = fun(failedResults: List<RuleResult>, data: DomainData): ServiceResult = ServiceResult(
        failedResults = failedResults,
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        actions = listOf(),
      )

      val result = rulesService.buildResults(ruleSetResult, data, passStatus, guidanceFailStatus, failFunc)
      val expectedResult = ServiceResult(
        failedResults = listOf(),
        serviceStatus = ServiceStatus.CONFIRMED,
        actions = listOf("Action1"),
      )
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset has guidance fail so returns guidanceFailStatus with failed results and an action`() {
      val data = DomainData(
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
        tier = "A1",
      )
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.PASS,
        results = listOf(
          RuleResult(
            description = "rule 1",
            isGuidance = true,
            ruleStatus = RuleStatus.FAIL,
            potentialAction = "Action1",
          ),
        ),
      )
      val passStatus = ServiceStatus.CONFIRMED
      val guidanceFailStatus = ServiceStatus.UPCOMING
      val failFunc = fun(failedResults: List<RuleResult>, data: DomainData): ServiceResult = ServiceResult(
        failedResults = failedResults,
        serviceStatus = ServiceStatus.UPCOMING,
        actions = listOf("Action1"),
      )

      val result = rulesService.buildResults(ruleSetResult, data, passStatus, guidanceFailStatus, failFunc)
      val expectedResult = ServiceResult(
        failedResults = listOf(),
        serviceStatus = ServiceStatus.CONFIRMED,
        actions = listOf("Action1"),
      )
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `ruleset fails so returns failFunc with failed results and no actions`() {
      val data = DomainData(
        sex = male,
        releaseDate = OffsetDateTime.now().plusMonths(7),
        tier = "A1",
      )
      val ruleSetResult = RuleSetResult(
        ruleSetStatus = RuleSetStatus.FAIL,
        results = listOf(
          RuleResult(
            description = "rule 1",
            isGuidance = false,
            ruleStatus = RuleStatus.FAIL,
            potentialAction = "Action1",
          ),
        ),
      )
      val passStatus = ServiceStatus.CONFIRMED
      val guidanceFailStatus = ServiceStatus.UPCOMING
      val failFunc = fun(failedResults: List<RuleResult>, data: DomainData): ServiceResult = ServiceResult(
        failedResults = failedResults,
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        actions = listOf(),
      )

      val result = rulesService.buildResults(ruleSetResult, data, passStatus, guidanceFailStatus, failFunc)
      val expectedResult = ServiceResult(
        failedResults = ruleSetResult.results,
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        actions = listOf(),
      )
      assertThat(result).isEqualTo(expectedResult)
    }
  }

  @Nested
  inner class RunRulesEngineForCas1 {
    @Nested
    inner class DoesNotFailCas1StatusRuleSet {
      @Test
      fun `candidate eligible and placement allocated so status is CONFIRMED`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1",
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            submittedAt = OffsetDateTime.now(),
            status = ApprovedPremisesApplicationStatus.PlacementAllocated,
          ),
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(),
          serviceStatus = ServiceStatus.CONFIRMED,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `candidate not eligible and placement allocated so status is CONFIRMED`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1S",
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            submittedAt = OffsetDateTime.now(),
            status = ApprovedPremisesApplicationStatus.PlacementAllocated,
          ),
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(),
          serviceStatus = ServiceStatus.CONFIRMED,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `candidate eligible and placement not allocated but application submitted so status is SUBMITTED`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1",
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            submittedAt = OffsetDateTime.now(),
            status = ApprovedPremisesApplicationStatus.Started,
          ),
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(
            RuleResult(
              description = "FAIL if candidate has no upcoming placement",
              ruleStatus = RuleStatus.FAIL,
              isGuidance = true,
              potentialAction = null,
            ),
          ),
          serviceStatus = ServiceStatus.SUBMITTED,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }
    }

    @Nested
    inner class FailsCas1StatusRuleSetButEligible {
      @Test
      fun `placement allocated but application not submitted so status is UPCOMING`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1",
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            submittedAt = null,
            status = ApprovedPremisesApplicationStatus.PlacementAllocated,
          ),
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(),
          serviceStatus = ServiceStatus.UPCOMING,
          actions = listOf("Start approved premise referral in 31 days"),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `no application so status is UPCOMING`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1",
          cas1Application = null,
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(),
          serviceStatus = ServiceStatus.UPCOMING,
          actions = listOf("Start approved premise referral in 31 days"),
        )
        assertThat(result).isEqualTo(expectedResult)
      }
    }

    @Nested
    inner class NotEligible {
      @Test
      fun `S tier candidate so status is NOT_ELIGIBLE`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "A1S",
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(
            RuleResult(
              description = "FAIL if candidate is S Tier",
              ruleStatus = RuleStatus.FAIL,
              isGuidance = false,
            ),
          ),
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `Male risk level too low so status is NOT_ELIGIBLE`() {
        val data = DomainData(
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "D1",
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(
            RuleResult(
              description = "FAIL if candidate is Male and is not Tier A3 - B1",
              ruleStatus = RuleStatus.FAIL,
              isGuidance = false,
            ),
          ),
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `Female risk level too low so status is NOT_ELIGIBLE`() {
        val data = DomainData(
          sex = female,
          releaseDate = OffsetDateTime.now().plusMonths(7),
          tier = "D1",
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(
            RuleResult(
              description = "FAIL if candidate is Female and is not Tier A3 - C3",
              ruleStatus = RuleStatus.FAIL,
              isGuidance = false,
            ),
          ),
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          actions = listOf(),
        )
        assertThat(result).isEqualTo(expectedResult)
      }

      @Test
      fun `Too close to release date so status is NOT_STARTED`() {
        val data = DomainData(
          sex = female,
          releaseDate = OffsetDateTime.now().plusMonths(5),
          tier = "A1",
        )

        val result = rulesService.runRulesEngineForCas1(data)
        val expectedResult = ServiceResult(
          failedResults = listOf(
            RuleResult(
              description = "FAIL if candidate is within 6 months of release date",
              ruleStatus = RuleStatus.FAIL,
              isGuidance = true,
              potentialAction = "Start approved premise referral",
            ),
          ),
          serviceStatus = ServiceStatus.NOT_STARTED,
          actions = listOf("Start approved premise referral"),
        )
        assertThat(result).isEqualTo(expectedResult)
      }
    }
  }
}
