package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotMarkedAsArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.Instant

class Cas2NotMarkedAsArrivedRuleTest {
  private val rule = Cas2NotMarkedAsArrivedRule()
  private val description = "FAIL if the CAS2 arrival timestamp is missing"

  @Test
  fun `arrival timestamp present passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
            markedArrivedDateTime = Instant.parse("2026-11-10T12:00:00Z"),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing arrival timestamp fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(
            latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
            markedArrivedDateTime = null,
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing submitted application fails`() {
    val result = rule.evaluate(
      buildDomainData(cas2Application = buildCas2Application(submittedApplication = null)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing application fails`() {
    val result = rule.evaluate(buildDomainData(cas2Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
