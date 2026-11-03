package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.arrived.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2NotArrivedRuleTest {
  private val rule = Cas2NotArrivedRule()
  private val description = "FAIL if referral status is not Awaiting Arrival"

  @Test
  fun `assessment is awaiting arrival so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL),
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @NullSource
  @EnumSource(value = Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["AWAITING_ARRIVAL"])
  fun `assessment is not awaiting arrival so rule fails`(status: Cas2AssessmentStatus?) {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(
          latestAssessmentStatus = status,
          cancelledReason = "Cancelled".takeIf { status == Cas2AssessmentStatus.CANCELLED },
          offerDeclinedReason = "Declined".takeIf { status == Cas2AssessmentStatus.OFFER_DECLINED },
        ),
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `application is unsubmitted so rule fails`() {
    val data = buildDomainData(cas2Application = buildCas2Application(submittedApplication = null))

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `application is missing so rule fails`() {
    assertThat(rule.evaluate(buildDomainData(cas2Application = null)))
      .isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
