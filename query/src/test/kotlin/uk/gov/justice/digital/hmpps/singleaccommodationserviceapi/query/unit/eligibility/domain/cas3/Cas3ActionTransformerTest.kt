package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.PROVIDE_INFORMATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.buildCas3Action
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas3ActionTransformerTest {
  private val crn = "ABC234"
  private val clock = MutableClock()
  private val tier = TierScore.A1

  @Test
  fun `Build action when release date is 3 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(3))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = RuleAction(START_CAS3_REFERRAL)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 28 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(28))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = RuleAction(START_CAS3_REFERRAL)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 29 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(29))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = RuleAction("$START_CAS3_REFERRAL in 1 day", true)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 60 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(60))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = RuleAction("$START_CAS3_REFERRAL in 32 days", true)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date null and error and no application`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = null,
    )
    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Release date for crn: ABC234 is null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3PlacementStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED", "CLOSED"])
  fun `Build action when application is PLACED but placement not completed`(status: Cas3PlacementStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas3ApplicationStatus.PLACED,
      placementStatus = status,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = cas3Application
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(RuleAction(CREATE_PLACEMENT))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["AWAITING_PLACEMENT", "PENDING"])
  fun `Build action when application needs to create a placement`(status: Cas3ApplicationStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = cas3Application
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(RuleAction(CREATE_PLACEMENT))
  }

  @Test
  fun `Build action when application is REQUESTED_FURTHER_INFORMATION`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        placementStatus = null
      )
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(RuleAction(PROVIDE_INFORMATION))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["IN_PROGRESS", "SUBMITTED", "REJECTED", "INAPPLICABLE", "WITHDRAWN"])
  fun `Build action when application is not suitable`(status: Cas3ApplicationStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusDays(10),
      cas3Application = cas3Application
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(RuleAction(START_CAS3_REFERRAL))
  }

  @Test
  fun `Error when application status is PLACED but placement status is null`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.PLACED,
        placementStatus = null
      )
    )

    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Invalid placement status: null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3PlacementStatus::class, names = ["PROVISIONAL", "CONFIRMED", "ARRIVED"])
  fun `Error when application status is PLACED but placement status is completed`(status: Cas3PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.PLACED,
        placementStatus = status
      )
    )

    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Invalid placement status: $status")
  }
}
