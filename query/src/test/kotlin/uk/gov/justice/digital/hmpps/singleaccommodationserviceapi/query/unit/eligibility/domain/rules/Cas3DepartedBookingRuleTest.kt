package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3DepartedBookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3DepartedBookingRuleTest {
  private val rule = Cas3DepartedBookingRule()
  private val description = "FAIL if the CAS3 booking status is 'Departed'"

  @Test
  fun `departed booking fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            latestBooking = buildCas3LatestBooking(status = Cas3BookingStatus.DEPARTED),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3BookingStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["DEPARTED"])
  fun `booking with any other status passes`(status: Cas3BookingStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            latestBooking = buildCas3LatestBooking(status = status),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing booking passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(latestBooking = null),
        ),
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
