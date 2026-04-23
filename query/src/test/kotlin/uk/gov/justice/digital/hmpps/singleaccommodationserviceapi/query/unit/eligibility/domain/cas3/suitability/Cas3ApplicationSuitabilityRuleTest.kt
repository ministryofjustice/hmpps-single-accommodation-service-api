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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3ApplicationSuitabilityRuleTest {
  private val description = "FAIL if CAS3 application is rejected"

  @Test
  fun `application has rejected status so rule fails`() {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.REJECTED,
        assessmentStatus = null,
        bookingStatus = null,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["REJECTED"])
  fun `application does not have rejected status so rule passes`(applicationStatus: Cas3ApplicationStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = applicationStatus,
        assessmentStatus = null,
        bookingStatus = null,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application has assessment status so rule passes`() {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
        bookingStatus = null,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application has booking status so rule passes`() {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        assessmentStatus = null,
        bookingStatus = Cas3BookingStatus.ARRIVED,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

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

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationSuitabilityRule().description).isEqualTo(description)
  }
}
