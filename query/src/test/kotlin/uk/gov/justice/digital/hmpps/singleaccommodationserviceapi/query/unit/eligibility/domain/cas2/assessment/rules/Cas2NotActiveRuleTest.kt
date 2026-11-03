package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.assessment.rules

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2NotActiveRuleTest {
  private val rule = Cas2NotActiveRule()
  private val description = "FAIL if assessment is inactive"

  @ParameterizedTest
  @EnumSource(value = Cas2AssessmentStatus::class, names = ["WITHDRAWN", "CANCELLED", "OFFER_DECLINED"])
  fun `assessment is inactive so rule fails`(status: Cas2AssessmentStatus) {
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

  @ParameterizedTest
  @NullSource
  @EnumSource(value = Cas2AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["WITHDRAWN", "CANCELLED", "OFFER_DECLINED"])
  fun `assessment is active so rule passes`(status: Cas2AssessmentStatus?) {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = status),
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `application is unsubmitted so rule passes`() {
    val data = buildDomainData(cas2Application = buildCas2Application(submittedApplication = null))

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `application is missing so rule passes`() {
    assertThat(rule.evaluate(buildDomainData(cas2Application = null)))
      .isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
