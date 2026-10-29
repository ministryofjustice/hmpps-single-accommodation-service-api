package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrExpiredRuleTest {
  private val description = "FAIL if the DTR submission date is missing or more than 26 weeks ago"

  private val clock = MutableClock()

  @Test
  fun `person fails when DTR submission date is missing`() {
    val data = buildDomainData(
      dutyToRefer = null,
    )

    val result = DtrExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.DTR_REFERRAL_EXPIRED,
      ),
    )
  }

  @Test
  fun `person fails when DTR is there but submission date is missing`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(submission = null),
    )

    val result = DtrExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.DTR_REFERRAL_EXPIRED,
      ),
    )
  }

  @Test
  fun `person passes when DTR submission date is exactly 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(
        submission = buildDtrSubmission(
          submissionDate = LocalDate.now(clock).minusWeeks(26),
        ),
      ),
    )

    val result = DtrExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person passes when DTR submission date is within 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(
        submission = buildDtrSubmission(
          submissionDate = LocalDate.now(clock).minusDays(100),
        ),
      ),
    )

    val result = DtrExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person fails when DTR submission date is more than 26 weeks ago`() {
    val data = buildDomainData(
      dutyToRefer = buildDutyToReferDto(
        submission = buildDtrSubmission(
          submissionDate = LocalDate.now(clock).minusDays(200),
        ),
      ),
    )

    val result = DtrExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.DTR_REFERRAL_EXPIRED,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrExpiredRule(clock).description)
      .isEqualTo("FAIL if the DTR submission date is missing or more than 26 weeks ago")
  }
}
