package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.arrived.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.rules.Cas2NotMarkedAsArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.Instant

class Cas2NotMarkedAsArrivedRuleTest {
  private val rule = Cas2NotMarkedAsArrivedRule()
  private val description = "FAIL if not marked as arrived"

  @Test
  fun `arrival is recorded so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(
          latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
          markedArrivedDateTime = Instant.parse("2025-01-01T00:00:00Z"),
        ),
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `arrival is not recorded so rule fails`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(markedArrivedDateTime = null),
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
