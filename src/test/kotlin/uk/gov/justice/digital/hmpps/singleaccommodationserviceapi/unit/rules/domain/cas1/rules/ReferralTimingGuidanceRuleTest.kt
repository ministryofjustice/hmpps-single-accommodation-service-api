package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.ReferralTimingGuidanceRule
import java.time.OffsetDateTime

class ReferralTimingGuidanceRuleTest {
  private val rule = ReferralTimingGuidanceRule()
  private val male = Sex(code = "M", description = "Male")

  @Test
  fun `referral is not completed, release date within 6 months so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `referral is not completed, release date more than 6 months away so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = null,
      releaseDate = OffsetDateTime.now().plusMonths(7),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `referral made within 6 months of release so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = OffsetDateTime.now(),
      releaseDate = OffsetDateTime.now().plusMonths(5),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `referral made more than 6 months before release so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      referralDate = OffsetDateTime.now(),
      releaseDate = OffsetDateTime.now().plusMonths(8),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo("Referral should be completed 6 months prior to release date")
  }

  @Test
  fun `rule is applicable to CAS 1`() {
    assertThat(rule.services).contains(ServiceType.CAS1)
  }
}
