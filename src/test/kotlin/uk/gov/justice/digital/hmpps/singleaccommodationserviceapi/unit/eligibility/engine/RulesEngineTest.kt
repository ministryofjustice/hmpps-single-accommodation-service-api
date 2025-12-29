package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.MaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.NonMaleRiskRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.STierRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules.WithinSixMonthsOfReleaseRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.DefaultRuleSetEvaluator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.engine.RulesEngine
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildSex
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1RuleSet::class,
    STierRule::class,
    MaleRiskRule::class,
    NonMaleRiskRule::class,
    WithinSixMonthsOfReleaseRule::class,
    ClockConfig::class,
  ],
)
class RulesEngineTest {
  private val defaultRulesEngine = RulesEngine(DefaultRuleSetEvaluator())
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)

  @Autowired
  private lateinit var cas1RuleSet: Cas1RuleSet

  @Test
  fun `rules engine passes cas1 rules`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(7),
    )

    val result = defaultRulesEngine.execute(cas1RuleSet, data)

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
      tier = TierScore.C1S,
      sex = buildSex(SexCode.F),
      releaseDate = LocalDate.now().plusMonths(7),
    )

    val result = defaultRulesEngine.execute(cas1RuleSet, data)

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
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(4),
    )

    val result = defaultRulesEngine.execute(cas1RuleSet, data)

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
      tier = TierScore.A1S,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(4),
    )

    val result = defaultRulesEngine.execute(cas1RuleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(),
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    )
    assertThat(result).isEqualTo(expectedResult)
  }
}
