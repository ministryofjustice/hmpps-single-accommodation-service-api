package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate
import java.util.UUID

class NextAccommodationRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @Test
  fun `candidate passes when next accommodation is null`() {
    val data = buildDomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      nextAccommodationId = null,
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when next accommodation exists`() {
    val data = buildDomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      nextAccommodationId = UUID.randomUUID(),
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
