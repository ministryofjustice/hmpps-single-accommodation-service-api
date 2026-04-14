package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrExpiredReferralRuleTest {

  private val clock = MutableClock()

  @Test
  fun `candidate fails when DTR submission date is missing`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate fails when DTR is there but submission date is missing`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(submission = null),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate passes when DTR submission date is exactly 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = LocalDate.now(clock).minusWeeks(26))),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when DTR submission date is within 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = LocalDate.now(clock).minusDays(100))),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when DTR submission date is more than 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(submission = buildDtrSubmission(submissionDate = LocalDate.now(clock).minusDays(200))),
    )

    val result = DtrExpiredReferralRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrExpiredReferralRule(clock).description)
      .isEqualTo("FAIL if DTR is submitted more than 26 weeks ago.")
  }
}
