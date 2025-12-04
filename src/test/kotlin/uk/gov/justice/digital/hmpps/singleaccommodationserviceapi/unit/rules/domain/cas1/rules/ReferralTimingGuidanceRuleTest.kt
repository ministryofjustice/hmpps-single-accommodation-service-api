package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
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
  fun `release date within 6 months so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(4),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `release date more than 6 months away so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(7),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `Within 6 months of release so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(5),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `More than 6 months before release so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(8),
    )
    val result = rule.evaluate(data)
    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo("FAIL if candidate is within 6 months of release date")
  }

  @Test
  fun `rule is applicable to CAS 1`() {
    assertThat(rule.services).contains(ServiceType.CAS1)
  }

  @Nested
  inner class BuildAction {
    @Test
    fun `Build action when release date is 3 days in future`() {
      val releaseDate = OffsetDateTime.now().plusDays(3)
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = releaseDate,
      )
      val result = rule.buildAction(true, data)
      val expectedResult = "Start approved premise referral"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 7 months in future`() {
      val releaseDate = OffsetDateTime.now().plusMonths(7)
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = releaseDate,
      )
      val result = rule.buildAction(false, data)
      val expectedResult = "Start approved premise referral in 31 days"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months in future`() {
      val releaseDate = OffsetDateTime.now().plusMonths(6)
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = releaseDate,
      )
      val result = rule.buildAction(false, data)
      val expectedResult = "Start approved premise referral"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 1 day in future`() {
      val releaseDate = OffsetDateTime.now().plusMonths(6).plusDays(1)
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = releaseDate,
      )
      val result = rule.buildAction(false, data)
      val expectedResult = "Start approved premise referral in 1 day"
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 2 days in future`() {
      val releaseDate = OffsetDateTime.now().plusMonths(6).plusDays(2)
      val data = DomainData(
        tier = "A1",
        sex = male,
        releaseDate = releaseDate,
      )
      val result = rule.buildAction(false, data)
      val expectedResult = "Start approved premise referral in 2 days"
      assertThat(result).isEqualTo(expectedResult)
    }
  }
}
