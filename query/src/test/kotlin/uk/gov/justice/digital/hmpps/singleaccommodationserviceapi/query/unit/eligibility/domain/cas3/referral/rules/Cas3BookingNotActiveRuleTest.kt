package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.referral.rules

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3BookingNotActiveRuleTest {
  private val description = "FAIL if the booking is departed or closed"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3BookingStatus::class, names = ["CLOSED", "DEPARTED"])
  fun `application has expired booking status so rule fails`(bookingStatus: Cas3BookingStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplication(
          assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
          latestBooking = buildCas3LatestBooking(
            status = bookingStatus,
          ),
        ),
      ),
    )

    val result = Cas3BookingNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3BookingStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["CLOSED", "DEPARTED"])
  fun `application has non-expired booking status so rule passes`(bookingStatus: Cas3BookingStatus) {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplication(
          assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
          latestBooking = buildCas3LatestBooking(
            status = bookingStatus,
          ),
        ),
      ),
    )

    val result = Cas3BookingNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application has no booking status so rule passes`() {
    val data = buildDomainData(
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplication(
          assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
          latestBooking = buildCas3LatestBooking(
            status = null,
          ),
        ),
      ),
    )

    val result = Cas3BookingNotActiveRule().evaluate(data)

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

    val result = Cas3BookingNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3BookingNotActiveRule().description).isEqualTo(description)
  }
}
