package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3ApplicationRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3ApplicationRejectedRuleTest {
  private val rule = Cas3ApplicationRejectedRule()
  private val description = "FAIL if there is no CAS3 assessment and the CAS3 application status is Rejected"

  @Test
  fun `rejected application without assessment fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(applicationStatus = Cas3ApplicationStatus.REJECTED),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(Cas3AssessmentStatus::class)
  fun `rejected application with assessment passes`(status: Cas3AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.REJECTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = status,
            assessmentRejectionReason = if (status == Cas3AssessmentStatus.REJECTED) "reason" else null,
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["REJECTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `application with other status passes`(status: Cas3ApplicationStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(applicationStatus = status),
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
