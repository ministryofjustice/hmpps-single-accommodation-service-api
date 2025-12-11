package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.engine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityBaseTest
import java.time.OffsetDateTime

class RulesEngineTest : EligibilityBaseTest() {
  private val crn = "ABC234"

  @Test
  fun `rules engine passes cas1 rules`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(7),
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
      sex = female,
      releaseDate = OffsetDateTime.now().plusMonths(7),
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
      releaseDate = OffsetDateTime.now().plusMonths(4),
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
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )

    val result = defaultRulesEngine.execute(cas1RuleSet, data)

    val expectedResult = ServiceResult(
      actions = listOf(),
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
    )
    assertThat(result).isEqualTo(expectedResult)
  }
}
