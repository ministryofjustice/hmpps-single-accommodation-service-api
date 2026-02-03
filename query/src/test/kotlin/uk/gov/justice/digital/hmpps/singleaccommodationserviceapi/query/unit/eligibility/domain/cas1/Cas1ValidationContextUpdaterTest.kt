package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildDomainData
import java.time.LocalDate
import java.util.UUID

class Cas1ValidationContextUpdaterTest {
  private val updater = Cas1ValidationContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns context unchanged`() {
      val releaseDate = LocalDate.parse("2026-12-31")
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

      assertThat(result).isEqualTo(context)
    }
  }
}