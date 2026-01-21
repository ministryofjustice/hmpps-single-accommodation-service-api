package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.AWAIT_ASSESSMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_APPROVED_PREMISE_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas1ContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas1ContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds action using buildAction`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          id = applicationId,
          applicationStatus = Cas1ApplicationStatus.STARTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.CONFIRMED),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isNotNull()
      assertThat(result.currentResult.action?.text).isEqualTo(START_APPROVED_PREMISE_REFERRAL)
    }

    @Test
    fun `update calculates service status using toServiceStatus with application status`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.AWAITING_PLACEMENT,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
    }

    @Test
    fun `update sets suitableApplicationId from cas1Application id`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          id = applicationId,
          applicationStatus = Cas1ApplicationStatus.STARTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.suitableApplicationId).isEqualTo(applicationId)
    }

    @Test
    fun `update sets suitableApplicationId to null when cas1Application is null`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = null,
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.suitableApplicationId).isNull()
    }

    @Test
    fun `update returns updated context with new ServiceResult`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.AWAITING_PLACEMENT,
        ),
      )
      val originalContext = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(originalContext)

      assertThat(result.data).isEqualTo(originalContext.data)
      assertThat(result.currentResult).isNotEqualTo(originalContext.currentResult)
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
    }

    @Test
    fun `update handles AWAITING_ASSESSMENT status correctly`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.SUBMITTED)
      assertThat(result.currentResult.action?.text).isEqualTo(AWAIT_ASSESSMENT)
      assertThat(result.currentResult.action?.isUpcoming).isTrue()
    }

    @Test
    fun `update handles PLACEMENT_ALLOCATED status correctly`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
          placementStatus = uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus.DEPARTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
      assertThat(result.currentResult.action?.text).isEqualTo(CREATE_PLACEMENT)
    }

    @Test
    fun `update handles REJECTED status correctly`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.REJECTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.REJECTED)
    }

    @Test
    fun `update handles WITHDRAWN status correctly`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          applicationStatus = Cas1ApplicationStatus.WITHDRAWN,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.WITHDRAWN)
    }
  }
}