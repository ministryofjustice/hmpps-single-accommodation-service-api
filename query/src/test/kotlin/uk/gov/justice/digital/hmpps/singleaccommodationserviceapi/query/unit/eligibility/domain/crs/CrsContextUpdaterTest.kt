package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class CrsContextUpdaterTest {
  private val updater = CrsContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result`() {
      val data = buildDomainData(
        commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
          status = CrsStatus.COMPLETED,
          submissionDate = LocalDate.now(),
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isNotNull()
      assertThat(result.currentResult.action).isEqualTo(EligibilityKeys.COMPLETE_CRS_REFERRAL)
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.NOT_STARTED)
      assertThat(result.currentResult.link).isNotNull()
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.VIEW_REFER_AND_MONITOR)
      assertThat(result.currentResult.suitableApplicationId).isNull()
    }
  }
}
