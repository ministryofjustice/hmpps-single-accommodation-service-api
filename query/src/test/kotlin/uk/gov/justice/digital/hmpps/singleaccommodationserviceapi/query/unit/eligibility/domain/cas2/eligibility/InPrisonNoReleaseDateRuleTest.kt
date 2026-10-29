package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.InPrisonNoReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class InPrisonNoReleaseDateRuleTest {
  private val rule = InPrisonNoReleaseDateRule()
  private val description = "FAIL if person is in prison and has no release date"

  @Test
  fun `person in prison without a release date fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(isPrison = true),
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(code = "HMP"),
        endDate = null,
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IN_PRISON_NO_RELEASE_DATE,
      ),
    )
  }

  @Test
  fun `person in prison with a release date passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(isPrison = true),
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(code = "HMP"),
        endDate = LocalDate.parse("2025-01-01"),
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `person not in prison without an accommodation end date passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(isPrison = false),
      currentAccommodation = buildAccommodationSummaryDto(endDate = null),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `person without current accommodation passes`() {
    assertThat(rule.evaluate(buildDomainData(currentAccommodation = null))).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `person with missing accommodation type entity passes even with HMP code`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = null,
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(code = "HMP"),
        endDate = null,
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
