package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotRejectedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3NotRejectedRuleTest {
  private val rule = Cas3NotRejectedRule()
  private val description = "FAIL unless there is no booking and either the assessment is rejected, or there is no assessment and the application is rejected"

  @Test
  fun `rejected assessment with no booking passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = Cas3AssessmentStatus.REJECTED,
            assessmentRejectionReason = "Rejected",
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `rejected application with no assessment and no booking passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.REJECTED,
          submittedApplication = buildCas3SubmittedApplication(assessmentStatus = null),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3BookingStatus::class)
  fun `rejected assessment with a booking fails`(bookingStatus: Cas3BookingStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            assessmentStatus = Cas3AssessmentStatus.REJECTED,
            assessmentRejectionReason = "Rejected",
            latestBooking = buildCas3LatestBooking(status = bookingStatus),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3AssessmentStatus::class, names = ["REJECTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `rejected application with a non rejected assessment fails`(assessmentStatus: Cas3AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.REJECTED,
          submittedApplication = buildCas3SubmittedApplication(assessmentStatus = assessmentStatus),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["REJECTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `non rejected application with no assessment fails`(applicationStatus: Cas3ApplicationStatus) {
    val result = rule.evaluate(buildDomainData(cas3Application = buildCas3Application(applicationStatus = applicationStatus)))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing application fails`() {
    val result = rule.evaluate(buildDomainData(cas3Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
