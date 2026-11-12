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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotBedspaceOfferedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3NotBedspaceOfferedRuleTest {
  private val rule = Cas3NotBedspaceOfferedRule()
  private val description = "FAIL if the CAS3 booking status is not Provisional"

  @Test
  fun `provisional booking passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            latestBooking = buildCas3LatestBooking(status = Cas3BookingStatus.PROVISIONAL),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @EnumSource(value = Cas3BookingStatus::class, names = ["PROVISIONAL"], mode = EnumSource.Mode.EXCLUDE)
  fun `booking with other status fails`(status: Cas3BookingStatus) {
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

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing booking status fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(
            latestBooking = buildCas3LatestBooking(status = null),
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing booking fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.SUBMITTED,
          submittedApplication = buildCas3SubmittedApplication(latestBooking = null),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing submitted application fails`() {
    val result = rule.evaluate(
      buildDomainData(cas3Application = buildCas3Application(applicationStatus = Cas3ApplicationStatus.IN_PROGRESS)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing application fails`() {
    val result = rule.evaluate(buildDomainData(cas3Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
