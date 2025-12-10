package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine
import java.time.OffsetDateTime

class RulesEngineTest {
  val ruleSet = Cas1RuleSet()
  private val male = Sex(
    code = "M",
    description = "Male",
  )
  private val female = Sex(
    code = "F",
    description = "Female",
  )
  private val crn = "ABC234"

  private val defaultRuleSetEvaluator = DefaultRuleSetEvaluator()

  @Test
  fun `rules engine passes cas1 rules`() {
    val data = DomainData(
      crn = crn,
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(7),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(
        "Start approved premise referral in 31 days",
      ),
      serviceStatus = ServiceStatus.UPCOMING,
    )
    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails some cas1 rules`() {
    val data = DomainData(
      crn = crn,
      tier = "C1S",
      sex = female,
      releaseDate = OffsetDateTime.now().plusMonths(7),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(),
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    )
    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails just with a fail of actionable rule so should return NOT_STARTED`() {
    val data = DomainData(
      crn = crn,
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(
        "Start approved premise referral",
      ),
      serviceStatus = ServiceStatus.NOT_STARTED,
    )
    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `rules engine fails with a fail of actionable rule and a fail of non guidance rule so should return NOT_ELIGIBLE`() {
    val data = DomainData(
      crn = crn,
      tier = "A1S",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )

    val result = RulesEngine(defaultRuleSetEvaluator).execute(ruleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(),
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    )
    assertThat(result).isEqualTo(expectedResult)
  }
}
