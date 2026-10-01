package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.time.LocalDate
import java.util.UUID

class Cas3SuitabilityContextUpdaterTest {
  private val updater = Cas3SuitabilityContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val currentAccommodationEndDate = LocalDate.parse("2026-12-31")
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = currentAccommodationEndDate),
        cas3Application = buildCas3Application(
          id = applicationId,
          applicationStatus = Cas3ApplicationStatus.REJECTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResultNew(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatusNew.CAS3_REJECTED)
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.START_NEW_REFERRAL)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS3_START_REFERRAL)
      assertThat(result.currentResult.url).isNull()
    }

    @Test
    fun `in progress application does produce a referral link`() {
      val data = buildDomainData(
        cas3Application = buildCas3Application(
          id = UUID.randomUUID(),
          applicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResultNew(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatusNew.CAS3_NOT_SUBMITTED)
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.CONTINUE_REFERRAL)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS3_VIEW_REFERRAL)
      assertThat(result.currentResult.url).isNull()
    }
  }
}
