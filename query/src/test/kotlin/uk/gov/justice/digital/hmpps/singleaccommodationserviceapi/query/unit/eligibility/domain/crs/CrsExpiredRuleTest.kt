package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.time.ZoneOffset

class CrsExpiredRuleTest {
  private val description = "FAIL if CRS not within 12 weeks"
  private val clock = MutableClock()

  @Test
  fun `crs is exactly 12 weeks so passes`() {
    val data = buildDomainData(
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        sentAt = LocalDate.now(clock).minusWeeks(12).atStartOfDay().atOffset(ZoneOffset.UTC),
      ),
    )

    val result = CrsExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `crs is within 12 weeks so passes`() {
    val data = buildDomainData(
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        sentAt = LocalDate.now(clock).minusWeeks(11).atStartOfDay().atOffset(ZoneOffset.UTC),
      ),
    )

    val result = CrsExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `crs is submitted longer than 12 weeks ago so fails`() {
    val data = buildDomainData(
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        sentAt = LocalDate.now(clock).minusWeeks(13).atStartOfDay().atOffset(ZoneOffset.UTC),
      ),
    )

    val result = CrsExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.CRS_EXPIRED,
      ),
    )
  }

  @Test
  fun `crs is missing so fails`() {
    val data = buildDomainData(
      commissionedRehabilitativeServices = null,
    )

    val result = CrsExpiredRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.CRS_EXPIRED,
      ),
    )
  }
}
