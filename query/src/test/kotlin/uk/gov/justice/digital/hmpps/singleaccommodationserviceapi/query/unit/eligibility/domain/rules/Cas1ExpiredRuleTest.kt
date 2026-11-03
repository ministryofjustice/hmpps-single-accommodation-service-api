package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas1ExpiredRuleTest {
  private val description = "FAIL if the referral is expired and there is no pending or active placement"

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1ApplicationStatus::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = [
      "EXPIRED",
    ],
  )
  fun `application is not expired so rule passes`(status: Cas1ApplicationStatus) {
    val cas1Application = buildCas1ApplicationV2(status = status, id = UUID.randomUUID())

    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(cas1Application, currentOutcome = null),
    )

    val result = Cas1ExpiredRule().evaluate(data)

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
  fun `application is expired and current outcome placement is UPCOMING or ARRIVED so rule passes`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.EXPIRED, id = UUID.randomUUID()),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = status),
        ),
      ),
    )

    val result = Cas1ExpiredRule().evaluate(data)

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
  fun `application is expired and current outcome placement is not UPCOMING or ARRIVED so rule fails`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.EXPIRED, id = UUID.randomUUID()),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = status),
        ),
      ),
    )

    val result = Cas1ExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `application is expired and current outcome has no placement so rule fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.EXPIRED),
        currentOutcome = null,
      ),
    )

    val result = Cas1ExpiredRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
