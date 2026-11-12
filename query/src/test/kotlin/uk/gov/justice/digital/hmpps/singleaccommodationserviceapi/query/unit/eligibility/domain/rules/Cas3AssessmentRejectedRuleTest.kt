package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3AssessmentRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3AssessmentRejectedRuleTest {
  private val rule = Cas3AssessmentRejectedRule()
  private val description = "FAIL if the CAS3 assessment status is Rejected"

  @Test
  fun `rejected assessment fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = Cas3AssessmentStatus.REJECTED,
            assessmentRejectionReason = "reason",
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3AssessmentStatus::class, names = ["REJECTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `assessment with other status passes`(status: Cas3AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = status,
            assessmentRejectionReason = if (status == Cas3AssessmentStatus.REJECTED) "reason" else null,
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing assessment status passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(assessmentStatus = null),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing submitted application passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(applicationStatus = Cas3ApplicationStatus.IN_PROGRESS),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing application passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = null,
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
