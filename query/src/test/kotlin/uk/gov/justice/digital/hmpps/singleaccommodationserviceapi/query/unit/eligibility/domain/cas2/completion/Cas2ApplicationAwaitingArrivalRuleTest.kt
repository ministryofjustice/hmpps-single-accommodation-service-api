package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationAwaitingArrivalRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2ApplicationAwaitingArrivalRuleTest {
  private val description = "FAIL if application is awaiting arrival"

  @Test
  fun `application is complete so rule passes`() {
    val cas2Application = buildCas2Application(
      submittedApplication = buildCas2SubmittedApplicationSummary(
        latestAssessmentStatus = "awaitingArrival",
      ),
    )

    val data = buildDomainData(
      cas2Application = cas2Application,
    )

    val result = Cas2ApplicationAwaitingArrivalRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application not completed so rule fails`() {
    val cas2Application = buildCas2Application(
      submittedApplication = buildCas2SubmittedApplicationSummary(
        latestAssessmentStatus = "STARTED",
      ),
    )

    val data = buildDomainData(
      cas2Application = cas2Application,
    )

    val result = Cas2ApplicationAwaitingArrivalRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
