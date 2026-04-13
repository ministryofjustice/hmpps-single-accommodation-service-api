package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.eligibility.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class DtrStatusRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @Test
  fun `candidate passes when DTR status is submitted`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      dtrStatus = "submitted",
    )

    val result = DtrStatusRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR status is not submitted`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      dtrStatus = null,
    )

    val result = DtrStatusRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrStatusRule().description)
      .isEqualTo("FAIL if DTR status is not submitted")
  }
}
