package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.completion

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.completion.PaCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PaCompletionContextUpdaterTest {
  private val updater = PaCompletionContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result`() {
      val data = buildDomainData()
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      Assertions.assertThat(result.currentResult.action).isEqualTo(EligibilityKeys.ADD_AND_CONFIRM_PROPOSED_ADDRESS)
      Assertions.assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.NOT_STARTED)
      Assertions.assertThat(result.currentResult.link).isNull()
    }
  }
}
