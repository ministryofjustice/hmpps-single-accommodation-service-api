package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.referral.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotStartedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2NotStartedRuleTest {
  private val rule = Cas2NotStartedRule()
  private val description = "FAIL if the referral is not started"

  @Test
  fun `application is missing so rule fails`() {
    assertThat(rule.evaluate(buildDomainData(cas2Application = null)))
      .isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `application exists but is unsubmitted so rule passes`() {
    val data = buildDomainData(cas2Application = buildCas2Application(submittedApplication = null))

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `application is submitted so rule passes`() {
    val data = buildDomainData(
      cas2Application = buildCas2Application(submittedApplication = buildCas2SubmittedApplicationSummary()),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
