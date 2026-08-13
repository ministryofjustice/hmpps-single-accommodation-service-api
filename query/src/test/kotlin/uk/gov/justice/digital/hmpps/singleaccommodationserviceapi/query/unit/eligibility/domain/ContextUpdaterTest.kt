package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult

class ContextUpdaterTest {
  private val context = EvaluationContext(
    data = buildDomainData(),
    currentResult = buildServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_APPROVED_PREMISE_APPLICATION),
    ),
  )

  private val failureReasons = listOf(
    FailureReason.CRS_EXPIRED,
    FailureReason.DTR_REFERRAL_EXPIRED,
  )

  @Nested
  inner class UpdateTests {
    @Test
    fun `update passes failure reasons to toServiceResult context`() {
      var reasonsSeenByToServiceResult: List<FailureReason> = emptyList()
      val updater = object : ContextUpdater() {
        override fun toServiceResult(context: EvaluationContext): ServiceResult {
          reasonsSeenByToServiceResult = context.currentResult.failureReasons
          return buildServiceResult(serviceStatus = ServiceStatus.CANNOT_START_YET)
        }
      }

      updater.update(context, failureReasons)

      assertThat(reasonsSeenByToServiceResult).containsExactlyElementsOf(failureReasons)
    }

    @Test
    fun `update does not propagate failure reasons by default`() {
      val updater = object : ContextUpdater() {
        override fun toServiceResult(context: EvaluationContext): ServiceResult = buildServiceResult(serviceStatus = ServiceStatus.CANNOT_START_YET)
      }

      val result = updater.update(context, failureReasons)

      assertThat(result.currentResult.failureReasons).isEmpty()
    }

    @Test
    fun `update propagates failure reasons when configured`() {
      val updater = object : ContextUpdater() {
        override val propagatesFailureReasons: Boolean = true

        override fun toServiceResult(context: EvaluationContext): ServiceResult = buildServiceResult(serviceStatus = ServiceStatus.CANNOT_START_YET)
      }

      val result = updater.update(context, failureReasons)

      assertThat(result.currentResult.failureReasons).containsExactlyElementsOf(failureReasons)
    }
  }

  @Nested
  inner class CompanionObjectTests {
    @Test
    fun `constant updater replaces service result and does not propagate failure reasons`() {
      val constantResult = buildServiceResult(serviceStatus = ServiceStatus.ACCEPTED)

      val result = ContextUpdater.constant(constantResult).update(context, failureReasons)

      assertThat(result.currentResult).isEqualTo(constantResult)
    }

    @Test
    fun `identity updater keeps current result fields and propagates failure reasons`() {
      val result = ContextUpdater.identity().update(context, failureReasons)

      assertThat(result.currentResult.serviceStatus).isEqualTo(context.currentResult.serviceStatus)
      assertThat(result.currentResult.action).isEqualTo(context.currentResult.action)
      assertThat(result.currentResult.failureReasons).containsExactlyElementsOf(failureReasons)
    }
  }
}
