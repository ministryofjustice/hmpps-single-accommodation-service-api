package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1ApplicationRelevantExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas1ApplicationRelevantExpiredRuleTest {
  private val description = "FAIL if expired application is not upcoming or arrived"

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1ApplicationStatus::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = [
      "EXPIRED",
    ],
  )
  fun `application is not expired so rule passes`(status: Cas1ApplicationStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationRelevantExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1PlacementStatus::class,
    names = [
      "ARRIVED",
      "UPCOMING",
    ],
  )
  fun `application is expired and UPCOMING or ARRIVED rule passes`(status: Cas1PlacementStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.EXPIRED,
      placementStatus = status,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationRelevantExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1PlacementStatus::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = [
      "ARRIVED",
      "UPCOMING",
    ],
  )
  fun `application is expired and not UPCOMING or ARRIVED rule fails`(status: Cas1PlacementStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.EXPIRED,
      placementStatus = status,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationRelevantExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `application is expired with no placement so rule fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.EXPIRED,
        placementStatus = null,
      ),
    )

    val result = Cas1ApplicationRelevantExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
