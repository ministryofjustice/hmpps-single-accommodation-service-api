package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas3ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas3ApplicationNotSuitableRuleTest {
  private val description = "FAIL if candidate has suitable CAS3 application"

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas3ApplicationStatus::class,
    names = [
      "SUBMITTED",
      "REQUESTED_FURTHER_INFORMATION",
    ],
  )
  fun `application is suitable so rule fails`(status: Cas3ApplicationStatus) {
    val cas3Application = buildCas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      assessmentStatus = null,
      bookingStatus = null,
    )

    val data = buildDomainData(
      cas3Application = cas3Application,
    )

    val result = Cas3ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.SUITABLE_CAS3_APPLICATION,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas3ApplicationStatus::class,
    names = [
      "REJECTED",
      "IN_PROGRESS",
    ],
  )
  fun `application does not have a suitable status so rule passes`(status: Cas3ApplicationStatus) {
    val cas3Application = buildCas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      assessmentStatus = null,
      bookingStatus = null,
    )

    val data = buildDomainData(
      cas3Application = cas3Application,
    )

    val result = Cas3ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application is not present so rule passes`() {
    val data = buildDomainData(
      cas3Application = null,
    )

    val result = Cas3ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
