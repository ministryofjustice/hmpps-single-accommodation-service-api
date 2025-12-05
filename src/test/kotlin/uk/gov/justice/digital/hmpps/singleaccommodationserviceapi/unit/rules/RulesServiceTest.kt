package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonerseach.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.RulesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration.RulesOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.orchestration.RulesOrchestrationService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class RulesServiceTest {
  @MockK
  lateinit var rulesOrchestrationService: RulesOrchestrationService

  @InjectMockKs
  lateinit var rulesService: RulesService

  @Nested
  inner class CalculateEligibilityForCas1 {
    @Test
    fun `calculate eligibility for cas 1`() {
      val crn = "X12345"
      val prisonerNumber = "A1234BC"
      val expectedTier = "A1"
      val expectedSex = Sex("M", "Male")
      val expectedReleaseDate = LocalDate.now().plusMonths(6)
      val expectedActions = listOf("Start approved premise referral")
      val expectedServiceStatus = ServiceStatus.UPCOMING

      val corePersonRecord = CorePersonRecord(
        sex = expectedSex,
        identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber)),
      )
      val tier = Tier(
        tierScore = expectedTier,
        UUID.randomUUID(),
        LocalDateTime.now(),
        null,
      )
      val prisoner = Prisoner(
        releaseDate = expectedReleaseDate,
      )
      val cprAndTierData = RulesOrchestrationDto(
        crn = crn,
        cpr = corePersonRecord,
        tier = tier,
      )
      every { rulesOrchestrationService.getCprAndTier(crn) } returns cprAndTierData
      every { rulesOrchestrationService.getPrisoner(prisonerNumber) } returns prisoner

      val result = rulesService.calculateEligibilityForCas1(crn)

      val expectedResult = ServiceResult(listOf(), expectedServiceStatus, expectedActions)
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

  @Nested
  inner class BuildDomainData {
    @Test
    fun `getDomainData returns correct DomainData from rules orchestration service`() {
      val crn = "X12345"
      val prisonerNumber = "A1234BC"
      val expectedTier = "A1"
      val expectedSex = Sex("M", "Male")
      val expectedReleaseDate = LocalDate.now().plusMonths(10)

      val corePersonRecord = CorePersonRecord(sex = expectedSex, identifiers = Identifiers(prisonNumbers = listOf(prisonerNumber)))
      val tier = Tier(tierScore = expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val prisoner = Prisoner(releaseDate = expectedReleaseDate)
      val orchestrationDto = RulesOrchestrationDto(crn, corePersonRecord, tier)

      every { rulesOrchestrationService.getCprAndTier(crn) } returns orchestrationDto
      every { rulesOrchestrationService.getPrisoner(prisonerNumber) } returns prisoner

      val result = rulesService.getDomainData(crn)

      assertThat(result.tier).isEqualTo(expectedTier)
      assertThat(result.sex).isEqualTo(expectedSex)
      assertThat(result.releaseDate).isEqualTo(expectedReleaseDate)
    }
  }
}
