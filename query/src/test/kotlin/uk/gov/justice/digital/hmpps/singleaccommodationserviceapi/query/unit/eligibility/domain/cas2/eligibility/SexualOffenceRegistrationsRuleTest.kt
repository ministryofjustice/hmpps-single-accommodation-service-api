package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.SexualOffenceRegistrationsRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class SexualOffenceRegistrationsRuleTest {
  private val rule = SexualOffenceRegistrationsRule()
  private val description = "FAIL if person has sexual offence registrations."

  @ParameterizedTest
  @ValueSource(ints = [1, 2])
  fun `person fails when there are sexual offence registrations`(count: Int) {
    val data = buildDomainData(
      sexualOffenceRegistrations = SexualOffenceRegistrations(
        crn = "X123456",
        sexualOffenceRegistrations = List(count) { mockk<SexualOffenceRegistration>() },
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.SEXUAL_OFFENCE_REGISTRATIONS_PRESENT,
      ),
    )
  }

  @Test
  fun `person passes when registrations are empty`() {
    val data = buildDomainData(
      sexualOffenceRegistrations = SexualOffenceRegistrations("X123456", emptyList()),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `person passes when registration list is null`() {
    val data = buildDomainData(
      sexualOffenceRegistrations = SexualOffenceRegistrations("X123456", null),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `person passes when registrations response is absent`() {
    assertThat(rule.evaluate(buildDomainData(sexualOffenceRegistrations = null))).isEqualTo(
      RuleResult(description = description, ruleStatus = RuleStatus.PASS),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
