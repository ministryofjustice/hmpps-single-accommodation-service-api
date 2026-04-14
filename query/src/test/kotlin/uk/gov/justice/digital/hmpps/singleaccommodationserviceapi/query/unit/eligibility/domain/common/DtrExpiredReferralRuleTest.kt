package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrExpiredReferralRuleTest {

  private val clock = MutableClock()

  @Test
  fun `candidate passes when DTR submission date is missing`() {
    val data = buildDomainData(
      dtrSubmissionDate = null,
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when DTR submission date is exactly 180 days ago`() {
    val data = buildDomainData(
      dtrSubmissionDate = LocalDate.now(clock).minusDays(180),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when DTR submission date is within 180 days ago`() {
    val data = buildDomainData(
      dtrSubmissionDate = LocalDate.now(clock).minusDays(100),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when DTR submission date is more than 180 days ago`() {
    val data = buildDomainData(
      dtrSubmissionDate = LocalDate.now(clock).minusDays(200),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    Assertions.assertThat(DtrExpiredReferralRule(clock).description)
      .isEqualTo("FAIL if DTR is submitted more than 180 days ago.")
  }
}
