package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1SubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1SubmittedRuleTest {
  private val description = "FAIL if the referral is submitted"

  @ParameterizedTest
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["STARTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `submitted referral fails`(status: Cas1ApplicationStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1Application(application = buildCas1ApplicationSummary(status = status)),
    )

    val result = Cas1SubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `unsubmitted referral passes`() {
    val data = buildDomainData(
      cas1Application = buildCas1Application(
        application = buildCas1ApplicationSummary(status = Cas1ApplicationStatus.STARTED),
      ),
    )

    val result = Cas1SubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing referral passes`() {
    val result = Cas1SubmittedRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
