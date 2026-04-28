package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3AssessmentSuitabilityRuleTest {
  private val description = "FAIL if CAS3 assessment is rejected or closed"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3AssessmentStatus::class, names = ["REJECTED", "CLOSED"])
  fun `application has unsuitable assessment status so rule fails`(assessmentStatus: Cas3AssessmentStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        assessmentStatus = assessmentStatus,
        bookingStatus = null,
      ),
    )

    val result = Cas3AssessmentSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3AssessmentStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["REJECTED", "CLOSED"])
  fun `application does not have unsuitable assessment status so rule passes`(assessmentStatus: Cas3AssessmentStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        assessmentStatus = assessmentStatus,
        bookingStatus = null,
      ),
    )

    val result = Cas3AssessmentSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3AssessmentStatus::class, names = ["REJECTED", "CLOSED"])
  fun `application has booking status so rule passes`(assessmentStatus: Cas3AssessmentStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        assessmentStatus = assessmentStatus,
        bookingStatus = Cas3BookingStatus.ARRIVED,
      ),
    )

    val result = Cas3AssessmentSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application is missing so rule passes`() {
    val data = buildDomainData(
      cas3Application = null,
    )

    val result = Cas3AssessmentSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3AssessmentSuitabilityRule().description).isEqualTo(description)
  }
}
