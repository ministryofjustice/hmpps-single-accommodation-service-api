package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.LinkKeys.START_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ServiceResultTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Cas1ServiceResultTransformerTest {
  private val crn = "ABC234"
  private val clock = MutableClock()

  @Test
  fun `Build action when release date is 3 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusDays(3))
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    val expectedResult = ActionKeys.START_APPROVED_PREMISE_APPLICATION
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        suitableApplicationId = null,
        action = expectedResult,
        link = START_APPLICATION,
      ),
    )
  }

  @Test
  fun `Build action when release date is 13 months in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusMonths(13))
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    val expectedResult = "${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 31 days"
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        suitableApplicationId = null,
        action = expectedResult,
        link = null,
      ),
    )
  }

  @Test
  fun `Build action when release date is 1 year in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusYears(1))
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    val expectedResult = ActionKeys.START_APPROVED_PREMISE_APPLICATION
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        suitableApplicationId = null,
        action = expectedResult,
        link = START_APPLICATION,
      ),
    )
  }

  @Test
  fun `Build action when release date is 1 year and 1 day in future and no application`() {
    val releaseDate = LocalDate.parse("2026-12-31")
    clock.setNow(releaseDate.minusYears(1).minusDays(1))
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    val expectedResult = "${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 1 day"
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        suitableApplicationId = null,
        action = expectedResult,
        link = null,
      ),
    )
  }

  @Test
  fun `Build action when release date is 1 year and 2 days in future and no application`() {
    val releaseDate = LocalDate.parse("2026-07-01")
    clock.setNow(releaseDate.minusYears(1).minusDays(2))
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    val expectedResult = "${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 2 days"
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        suitableApplicationId = null,
        action = expectedResult,
        link = null,
      ),
    )
  }

  @Test
  fun `Build action when release date null and no application so return NOT_ELIGIBLE`() {
    val data = DomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = null,
    )
    val result = Cas1ServiceResultTransformer.toCas1ServiceResult(data, clock)
    Assertions.assertThat(result).isEqualTo(
      ServiceResult(
        serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        suitableApplicationId = null,
        action = null,
        link = null,
      ),
    )
  }
}
