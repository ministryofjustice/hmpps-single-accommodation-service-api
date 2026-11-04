package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2NotActiveRuleTest {
  private val rule = Cas2NotActiveRule()
  private val description = "FAIL if assessment is present and inactive"

  @ParameterizedTest
  @EnumSource(value = Cas2AssessmentStatus::class, names = ["WITHDRAWN", "CANCELLED", "OFFER_DECLINED"])
  fun `inactive assessment fails`(status: Cas2AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = "Declined".takeIf { status == Cas2AssessmentStatus.OFFER_DECLINED },
            cancelledReason = "Cancelled".takeIf { status == Cas2AssessmentStatus.CANCELLED },
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(
    value = Cas2AssessmentStatus::class,
    names = ["WITHDRAWN", "CANCELLED", "OFFER_DECLINED"],
    mode = EnumSource.Mode.EXCLUDE,
  )
  fun `assessment that is not inactive passes`(status: Cas2AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = status),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing assessment status passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = null),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing submitted application passes`() {
    val result = rule.evaluate(
      buildDomainData(cas2Application = buildCas2Application(submittedApplication = null)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing application passes`() {
    val result = rule.evaluate(buildDomainData(cas2Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
