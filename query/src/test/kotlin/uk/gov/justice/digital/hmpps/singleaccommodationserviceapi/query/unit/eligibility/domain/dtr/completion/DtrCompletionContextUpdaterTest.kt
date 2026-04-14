package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DtrCompletionContextUpdaterTest {
  private val updater = DtrCompletionContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns context SUBMITTED by default`() {
      val data = buildDomainData()
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          action = EligibilityKeys.ADD_DTR_OUTCOME,
          link = EligibilityKeys.ADD_OUTCOME,
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }

    @Test
    fun `update returns context NOT_ACCEPTED if dtr status is NOT_ACCEPTED`() {
      val data = buildDomainData(
        dutyToRefer = buildDutyToReferDto(status = DtrStatus.NOT_ACCEPTED),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ACCEPTED),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
