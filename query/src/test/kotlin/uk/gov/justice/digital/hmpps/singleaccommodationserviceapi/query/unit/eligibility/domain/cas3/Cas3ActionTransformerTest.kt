package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.buildCas3Action
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas3ActionTransformerTest {
  private val crn = "ABC234"
  private val clock = MutableClock()
  private val tierScore = TierScore.A1

  @Test
  fun `Build action when release date is 3 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(3))
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = EligibilityKeys.START_CAS3_REFERRAL
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 28 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(28))
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = EligibilityKeys.START_CAS3_REFERRAL
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 29 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(29))
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = "${EligibilityKeys.START_CAS3_REFERRAL} in 1 day"
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 60 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(60))
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = buildCas3Action(data, clock)
    val expectedResult = "${EligibilityKeys.START_CAS3_REFERRAL} in 32 days"
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date null and error and no application`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = null,
    )
    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Release date for crn: ABC234 is null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3BookingStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED", "CLOSED"])
  fun `Build action when application is PLACED but booking not completed`(status: Cas3BookingStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas3ApplicationStatus.PLACED,
      bookingStatus = status,
    )

    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = cas3Application,
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(EligibilityKeys.CREATE_PLACEMENT)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["AWAITING_PLACEMENT", "PENDING"])
  fun `Build action when application needs to create a booking`(status: Cas3ApplicationStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      bookingStatus = null,
    )

    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = cas3Application,
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(EligibilityKeys.CREATE_PLACEMENT)
  }

  @Test
  fun `Build action when application is REQUESTED_FURTHER_INFORMATION`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        bookingStatus = null,
      ),
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(EligibilityKeys.PROVIDE_INFORMATION)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["IN_PROGRESS", "SUBMITTED", "REJECTED", "INAPPLICABLE", "WITHDRAWN"])
  fun `Build action when application is not suitable`(status: Cas3ApplicationStatus) {
    val cas3Application = Cas3Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      bookingStatus = null,
    )

    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusDays(10),
      cas3Application = cas3Application,
    )

    val result = buildCas3Action(data, clock)

    Assertions.assertThat(result).isEqualTo(EligibilityKeys.START_CAS3_REFERRAL)
  }

  @Test
  fun `Error when application status is PLACED but booking status is null`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.PLACED,
        bookingStatus = null,
      ),
    )

    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Invalid booking status: null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3BookingStatus::class, names = ["PROVISIONAL", "CONFIRMED", "ARRIVED"])
  fun `Error when application status is PLACED but booking status is completed`(status: Cas3BookingStatus) {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(1),
      cas3Application = Cas3Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.PLACED,
        bookingStatus = status,
      ),
    )

    Assertions.assertThatThrownBy { buildCas3Action(data, clock) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Invalid booking status: $status")
  }
}
