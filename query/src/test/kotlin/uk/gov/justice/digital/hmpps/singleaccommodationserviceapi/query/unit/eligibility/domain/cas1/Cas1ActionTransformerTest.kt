package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ActionTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas1ActionTransformerTest {
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
    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)
    val expectedResult = RuleAction(ActionKeys.START_APPROVED_PREMISE_APPLICATION)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 13 months in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusMonths(13))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ActionTransformer.buildCas1Action(data, clock, false)
    val expectedResult = RuleAction("${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 31 days", true)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 1 year in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusYears(1))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)
    val expectedResult = RuleAction(ActionKeys.START_APPROVED_PREMISE_APPLICATION)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 1 year and 1 day in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusYears(1).minusDays(1))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ActionTransformer.buildCas1Action(data, clock, false)
    val expectedResult = RuleAction("${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 1 day", true)
    Assertions.assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun `Build action when release date is 1 year and 2 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-07-01")
    clock.setNow(releaseDate.minusYears(1).minusDays(2))
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ActionTransformer.buildCas1Action(data, clock, false)
    val expectedResult = RuleAction("${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days", true)
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
    Assertions.assertThatThrownBy { Cas1ActionTransformer.buildCas1Action(data, clock, true) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Release date for crn: ABC234 is null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED"])
  fun `Build action when application is suitable (PLACEMENT_ALLOCATED) but not completed`(status: Cas1PlacementStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = status,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)

    Assertions.assertThat(result).isEqualTo(RuleAction(ActionKeys.CREATE_PLACEMENT))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["AWAITING_PLACEMENT", "PENDING_PLACEMENT_REQUEST"])
  fun `Build action when application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to create a placement`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)

    Assertions.assertThat(result).isEqualTo(RuleAction(ActionKeys.CREATE_PLACEMENT))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["AWAITING_ASSESSMENT", "UNALLOCATED_ASSESSMENT", "ASSESSMENT_IN_PROGRESS"])
  fun `Build action when application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to await assessment`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)

    Assertions.assertThat(result).isEqualTo(RuleAction(ActionKeys.AWAIT_ASSESSMENT, true))
  }

  @Test
  fun `Build action when application is REQUEST_FOR_FURTHER_INFORMATION`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        placementStatus = null,
      ),
    )

    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)

    Assertions.assertThat(result).isEqualTo(RuleAction(ActionKeys.PROVIDE_INFORMATION))
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["EXPIRED", "INAPPLICABLE", "STARTED", "REJECTED", "WITHDRAWN"])
  fun `Build action when application is not suitable`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = Cas1ActionTransformer.buildCas1Action(data, clock, true)

    Assertions.assertThat(result).isEqualTo(RuleAction(ActionKeys.START_APPROVED_PREMISE_APPLICATION))
  }

  @Test
  fun `Error when application status is PLACEMENT_ALLOCATED but placement status is null`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = null,
      ),
    )

    Assertions.assertThatThrownBy { Cas1ActionTransformer.buildCas1Action(data, clock, true) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Invalid placement status: null")
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["UPCOMING", "ARRIVED"])
  fun `Error when application status is PLACEMENT_ALLOCATED but placement status is UPCOMING or ARRIVED`(status: Cas1PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = status,
      ),
    )

    Assertions.assertThatThrownBy { Cas1ActionTransformer.buildCas1Action(data, clock, true) }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Invalid placement status: $status")
  }
}