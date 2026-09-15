package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2ApplicationSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.OffsetDateTime

class Cas2ApplicationSubmittedRuleTest {
  private val description = "FAIL if candidate does not have a submitted application"

  @Test
  fun `application is submitted so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = buildCas2SubmittedApplicationSummary(submittedAt = OffsetDateTime.now()),
      ),
    )

    val result = Cas2ApplicationSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application is not submitted so rule fails`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(
        submittedApplication = null,
      ),
    )

    val result = Cas2ApplicationSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
