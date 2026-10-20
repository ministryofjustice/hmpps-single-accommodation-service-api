package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationArrivedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.Instant

class Cas2ApplicationArrivedRuleTest {
  private val description = "FAIL if application is arrived"

  @Test
  fun `application is arrived so rule fails`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(
          latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
          markedArrivedDateTime = Instant.parse("2025-01-01T12:00:00Z"),
        ),
      ),
    )

    val result = Cas2ApplicationArrivedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `application is not arrived so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(
          latestAssessmentStatus = Cas2AssessmentStatus.AWAITING_ARRIVAL,
          markedArrivedDateTime = null,
        ),
      ),
    )

    val result = Cas2ApplicationArrivedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `submitted application is absent so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(submittedApplication = null),
    )

    val result = Cas2ApplicationArrivedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application is absent so rule passes`() {
    val data = buildDomainData(cas2Application = null)

    val result = Cas2ApplicationArrivedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
