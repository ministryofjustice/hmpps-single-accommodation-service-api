package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class NextAccommodationRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @Test
  fun `candidate passes when next accommodation is null`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      hasNextAccommodation = false,
    )

    val result = NextAccommodationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when next accommodation exists`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      hasNextAccommodation = true,
    )

    val result = NextAccommodationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    Assertions.assertThat(NextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
